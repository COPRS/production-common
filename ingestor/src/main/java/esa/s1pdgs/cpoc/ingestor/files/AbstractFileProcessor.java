package esa.s1pdgs.cpoc.ingestor.files;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.TreeCopier;
import esa.s1pdgs.cpoc.ingestor.files.model.FileDescriptor;
import esa.s1pdgs.cpoc.ingestor.files.services.AbstractFileDescriptorService;
import esa.s1pdgs.cpoc.ingestor.kafka.PublicationServices;
import esa.s1pdgs.cpoc.ingestor.status.AppStatus;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;;

public abstract class AbstractFileProcessor<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractFileProcessor.class);

    /**
     * Amazon S3 service for configuration files
     */
    private final ObsClient obsClient;

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
    
    /**
     * Pickup Directory
     */
    private final String pickupDirectory;
    
    /**
     * Backup Directory
     */
    private final String backupDirectory;

    public AbstractFileProcessor(final ObsClient obsClient,
            final PublicationServices<T> publisher,
            final AbstractFileDescriptorService extractor,
            final ProductFamily family,
            final AppStatus appStatus,
            final String pickupDirectory,
            final String backupDirectory) {
        this.obsClient = obsClient;
        this.publisher = publisher;
        this.extractor = extractor;
        this.family = family;
        this.appStatus = appStatus;
        this.pickupDirectory = pickupDirectory;
        this.backupDirectory = backupDirectory;
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
		final Reporting.Factory reportingFactory = new LoggerReporting.Factory("Ingestion");

		final Reporting reportProcessing = reportingFactory.newReporting(0);
		reportProcessing.begin(new ReportingMessage("Start processing of {}", file.getName()));
		this.appStatus.setProcessing(family);

		try {
			uploadAndPublish(reportingFactory, reportProcessing, file);
			delete(reportingFactory, file, 3);

		} catch (AbstractCodedException | SdkClientException e) {
			// is already logged
			this.appStatus.setError(family);
			try {
				copyToBackupDirectory(reportingFactory, file);
				delete(reportingFactory, file, 4);
			} catch (IOException e2) {
				LOGGER.warn("failed to backup file {}, not deleted", file.getName());
			}
		}
		reportProcessing.end(new ReportingMessage("End processing of {}", file.getName()));
		this.appStatus.setWaiting();
	}

	private final void uploadAndPublish(final Reporting.Factory reportingFactory, final Reporting reportProcessing,
			final File file) throws AbstractCodedException, SdkClientException {
		String productName = file.getName();
		final Reporting reportUpload = reportingFactory.newReporting(1);
		try {
			FileDescriptor descriptor = extractor.extractDescriptor(file);
			productName = descriptor.getProductName();
			reportUpload.begin(new ReportingMessage("Start uploading file {} in OBS", file.getName()));

			// Store in object storage
			upload(file, productName, descriptor);
			reportUpload.end(new ReportingMessage("End uploading file {} in OBS", file.getName()));

			// Send metadata
			publish(reportingFactory, productName, descriptor);

		} catch (IngestorIgnoredFileException e) {
			reportProcessing.intermediate(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			throw e;
		} catch (ObsAlreadyExist e) {
			reportUpload.error(new ReportingMessage("file {} already exist in OBS", file.getName()));
			throw e;
		} catch (ObsException e) {
			reportUpload.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			throw e;
		} catch (SdkClientException e) {
			reportUpload.error(new ReportingMessage("[code {}] {}", AbstractCodedException.ErrorCode.OBS_ERROR, e.getMessage()));
			throw e;
		} catch (AbstractCodedException e) {
			reportUpload.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
			throw e;
		}
	}

	private final void upload(final File file, final String productName, final FileDescriptor descriptor) throws ObsException, ObsAlreadyExist, ObsServiceException, SdkClientException {		
		if (obsClient.exists(new ObsObject(family,descriptor.getKeyObjectStorage()))) {
			throw new ObsAlreadyExist(family, descriptor.getProductName(), new Exception("File already exist in object storage"));
		}
		obsClient.uploadFile(family,descriptor.getKeyObjectStorage(), file);
	}

	private final void publish(final Reporting.Factory reportingFactory, final String productName, final FileDescriptor descriptor) throws MqiPublicationError {
		if (descriptor.isHasToBePublished()) {			
			final Reporting reportPublish= reportingFactory.newReporting(2);	
			reportPublish.begin(new ReportingMessage("Start publishing file in topic"));		    
			try {
				publisher.send(buildDto(descriptor));
				reportPublish.end(new ReportingMessage("End publishing file in topic"));
			} catch (MqiPublicationError e) {
				reportPublish.error(new ReportingMessage("[code {}] {}", e.getCode().getCode(), e.getLogMessage()));
				throw e;
			}
		}
	}
	
	private void copyToBackupDirectory(final Reporting.Factory reportingFactory, final File file) throws IOException {
		final Reporting reportBackup = reportingFactory.newReporting(3);
		reportBackup.begin(new ReportingMessage("Start copying file {} to {}", file.getName(), backupDirectory));
		try {
			Path pickupPath = new File(pickupDirectory).toPath();
			Path backupPath = new File(backupDirectory).toPath();
			
			Path productDir = pickupPath.relativize(file.toPath()).subpath(0, 1);
			Path dirToCopy = pickupPath.resolve(productDir);
			Path target = backupPath.resolve(productDir);
			TreeCopier tc = new TreeCopier(dirToCopy, target, true, false);
			Files.walkFileTree(dirToCopy, tc);
			
			reportBackup.end(new ReportingMessage("End copying file {} to {}", file.getName(), backupDirectory));
		} catch (IOException e) {
			reportBackup.error(new ReportingMessage(
					"Error copying file {} to {}: {}", file.getName(), backupDirectory, LogUtils.toString(e)));
			throw e;
		}
	}

	private final void delete(final Reporting.Factory reportingFactory, final File file, int reportingStep) {
		final Reporting reportDelete = reportingFactory.newReporting(reportingStep);
		reportDelete.begin(new ReportingMessage("Start removing file {}", file.getName()));
		try {
			Files.delete(Paths.get(file.getPath()));
			reportDelete.end(new ReportingMessage("End removing file {}", file.getName()));
		} catch (Exception e) {
			reportDelete.error(new ReportingMessage("[code {}] file {} cannot be removed from FTP storage: {}",
					AbstractCodedException.ErrorCode.INGESTOR_CLEAN.getCode(), file.getPath(), LogUtils.toString(e)));
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
