package esa.s1pdgs.cpoc.ingestor.files;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.messaging.Message;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.common.errors.obs.ObsAlreadyExist;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.processing.IngestorIgnoredFileException;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AbstractFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.PublicationServices;
import esa.s1pdgs.cpoc.ingestor.obs.ObsService;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

public abstract class AbstractFileProcessor<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractFileProcessor.class);

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsService obsService;

    /**
     * KAFKA producer on the topic "metadata"
     */
    private final PublicationServices<T> publisher;

    /**
     * Builder of file descriptors
     */
    private final AbstractFileDescriptorService extractor;

    /**
     * Product family processed
     */
    private final ProductFamily family;
    
    /**
     * Application status for archives
     */
    private final AppStatus appStatus;

    public AbstractFileProcessor(final ObsService obsService,
            final PublicationServices<T> publisher,
            final AbstractFileDescriptorService extractor,
            final ProductFamily family,
            final AppStatus appStatus) {
        this.obsService = obsService;
        this.publisher = publisher;
        this.extractor = extractor;
        this.family = family;
        this.appStatus = appStatus;
    }

    /**
     * Process configuration files.
     * <ul>
     * <li>Store in the object storage</li>
     * <li>Publish metadata</li>
     * </ul>
     * 
     * @param message
     */
    public void processFile(final Message<File> message) {
        final File file = message.getPayload();
        
        if (!isValidFile(file)) {
        	return;
        }        
        // Build model file
        handleFile(file); 
    }

	private final void handleFile(final File file) {
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "Ingestion")
        		.product(extractor.getFamily(), file.getName());
        
        final Reporting reportProcessing = reportingFactory.newReporting(0);        
        reportProcessing.reportStart("Start processing of " + file.getName());
        this.appStatus.setProcessing(family);
		
		try {			
		    uploadAndPublish(reportingFactory, reportProcessing, file);
		    delete(reportingFactory, file);
		
		// this is done to bypass product deletion on OBS upload errors. Don't know why, though
		} catch (ObsException ace) {
			// is already logged
		    this.appStatus.setError(family);
		}
	   reportProcessing.reportStop("End processing of " + file.getName());
       this.appStatus.setWaiting();
	}

	private final void uploadAndPublish(final Reporting.Factory reportingFactory,  final Reporting reportProcessing, final File file) throws ObsException {
		String productName = file.getName();
		try {
		    FileDescriptor descriptor = extractor.extractDescriptor(file);
		    productName = descriptor.getProductName();
		    reportingFactory.product(extractor.getFamily(), productName);
		    
		    final Reporting reportUpload = reportingFactory.newReporting(1);		    
		    reportUpload.reportStart("Start uploading file " + file.getName() +" in OBS");
		    		    
		    // Store in object storage
		    try {
				upload(file, productName, descriptor);
				reportUpload.reportStop("End uploading file " + file.getName() +" in OBS");
			} catch (ObsAlreadyExist ace) {
				reportUpload.reportError("file {} already exist in OBS", file.getName());
				return;
			} catch (ObsException e) {
				reportUpload.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
				throw e;
			}
		    // Send metadata
		    publish(reportingFactory, productName, descriptor);
		    
		} catch (IngestorIgnoredFileException ce) {
			reportProcessing.reportDebug("[code {}] {}", ce.getCode().getCode(), ce.getLogMessage());
		} catch (ObsException ace) {	
			// this is done to bypass product deletion on OBS upload errors. Don't know why, though
		    throw ace;
		} catch (AbstractCodedException ace) {
		    this.appStatus.setError(family);
		}
	}

	private final void upload(final File file, final String productName, final FileDescriptor descriptor) throws ObsException, ObsAlreadyExist {		
		if (obsService.exist(family,descriptor.getKeyObjectStorage())) {
			throw new ObsAlreadyExist(family, descriptor.getProductName(), new Exception("File already exist in object storage"));
		}
		obsService.uploadFile(family,descriptor.getKeyObjectStorage(), file);
	}

	private final void publish(final Reporting.Factory reportingFactory, final String productName, final FileDescriptor descriptor) throws MqiPublicationError {
		if (descriptor.isHasToBePublished()) {			
			final Reporting reportPublish= reportingFactory.newReporting(2);	
			reportPublish.reportStart("Start publishing file in topic");		    
			try {
				publisher.send(buildDto(descriptor));
				reportPublish.reportStop("End publishing file in topic");
			} catch (MqiPublicationError e) {
				reportPublish.reportError("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
				throw e;
			}
		}
	}

	private final void delete(final Reporting.Factory reportingFactory, final File file) {		
		final Reporting reportDelete = reportingFactory.newReporting(3);	
		reportDelete.reportStart("Start removing file " + file.getName());
		try {
		    Files.delete(Paths.get(file.getPath()));
			reportDelete.reportStop("End removing file " + file.getName());
		} catch (Exception e) {
			reportDelete.reportError(
					"[code {}] file {} cannot be removed from FTP storage: {}", 
		            AbstractCodedException.ErrorCode.INGESTOR_CLEAN .getCode(),
		            file.getPath(), 
		            e.getMessage()
		    );
		    this.appStatus.setError(family);
		}
	}
    
    private final boolean isValidFile(File file) {
        if (file.isDirectory()) {
            return false;
        } else {
            String path = file.getPath().toLowerCase();
            if (path.endsWith("manifest.safe")) {
                return true;
            } else if (path.endsWith(".safe") || path.endsWith("data") || path.endsWith("support")) {
                return false;
            }
        }
        return true;
    }

    protected abstract T buildDto(final FileDescriptor descriptor);

}
