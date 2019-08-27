package esa.s1pdgs.cpoc.mdcatalog.extraction;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.mdcatalog.ProcessConfiguration;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.mdcatalog.extraction.files.FileDescriptorBuilder;
import esa.s1pdgs.cpoc.mdcatalog.extraction.files.MetadataBuilder;
import esa.s1pdgs.cpoc.mdcatalog.extraction.xml.XmlConverter;
import esa.s1pdgs.cpoc.mdcatalog.status.AppStatus;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.AckMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

/**
 * @author Viveris Technologies
 * @param <T>
 */
public abstract class GenericExtractor<T> {
	
    interface ThrowingSupplier<E>
    {
    	E get() throws AbstractCodedException;
    }

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(GenericExtractor.class);

    /**
     * Elasticsearch services
     */
    protected final EsServices esServices;

    /**
     * Access to the MQI server
     */
    protected final GenericMqiClient mqiService;

    /**
     * Application status
     */
    protected final AppStatus appStatus;

    /**
     * Metadata builder
     */
    protected final MetadataBuilder mdBuilder;

    /**
     * Local directory for configurations files
     */
    protected final String localDirectory;

    /**
     * 
     */
    protected final MetadataExtractorConfig extractorConfig;

    /**
     * Builder of file descriptors
     */
    protected final FileDescriptorBuilder fileDescriptorBuilder;

    /**
     * Product category
     */
    protected final ProductCategory category;
    
    private final ErrorRepoAppender errorAppender;
    
    private final ProcessConfiguration processConfiguration;
    
    /**
     * @param esServices
     * @param mqiService
     * @param appStatus
     * @param localDirectory
     * @param extractorConfig
     * @param pattern
     */
    public GenericExtractor(final EsServices esServices,
            final GenericMqiClient mqiService, final AppStatus appStatus,
            final String localDirectory,
            final MetadataExtractorConfig extractorConfig, final String pattern,
            final ErrorRepoAppender errorAppender,
            final ProductCategory category,
            final ProcessConfiguration processConfiguration,
            final XmlConverter xmlConverter) {
        this.localDirectory = localDirectory;
        this.fileDescriptorBuilder =
                new FileDescriptorBuilder(this.localDirectory,
                        Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
        this.extractorConfig = extractorConfig;
        this.mdBuilder = new MetadataBuilder(this.extractorConfig, xmlConverter, localDirectory);
        this.esServices = esServices;
        this.mqiService = mqiService;
        this.appStatus = appStatus;
        this.category = category;
        this.errorAppender = errorAppender;
        this.processConfiguration = processConfiguration;
    }

    /**
     * Consume a message from a product category and extract metadata
     */
    public void genericExtract() {

        // ----------------------------------------------------------
        // Read Message
        // ----------------------------------------------------------
        LOGGER.trace("[MONITOR] [step 0] [{}] Waiting message", category);
        GenericMessageDto<T> message = null;
        try {
            message = mqiService.next(category);
            appStatus.setWaiting(category);
        } catch (AbstractCodedException ace) {
            LOGGER.error("[MONITOR] [step 0] [{}] [code {}] {}", category,
                    ace.getCode().getCode(), ace.getLogMessage());
            message = null;
            appStatus.setError(category, "NEXT_MESSAGE");
        }
        if (message == null || message.getBody() == null) {
            LOGGER.trace(
                    "[MONITOR] [step 0] [{}] No message received: continue",
                    category);
            return;
        }

        // ----------------------------------------------------------
        // Process Message
        // ----------------------------------------------------------
        T dto = message.getBody();
        
        final String productName = extractProductNameFromDto(dto);
        
        final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "MetadataExtraction")
        		.product(category.toString(), productName);
        
        final Reporting report = reportingFactory.newReporting(0);        
        report.begin("Starting metadata extraction");        
        appStatus.setProcessing(category, message.getIdentifier());
        
        FailedProcessingDto failedProc = new FailedProcessingDto();

        try { 	
        	
            final JSONObject metadata = extractMetadata(reportingFactory, message);
            if (!metadata.has("insertionTime")) {
            	metadata.put("insertionTime", DateUtils.formatToMetadataDateTimeFormat(LocalDateTime.now()));
            }
            
            final Reporting reportPublish = reportingFactory.newReporting(4);            
            reportPublish.begin("Start publishing metadata");

            try {
				if (!esServices.isMetadataExist(metadata)) {
				    esServices.createMetadata(metadata);
				}
			    reportPublish.end("End publishing metadata");
				
			} catch (Exception e) {
				reportPublish.error("[code {}] {}", ErrorCode.INTERNAL_ERROR.getCode(), LogUtils.toString(e));
				throw e;
			}
            // Acknowledge
            ackPositively(reportingFactory, message);

        } catch (AbstractCodedException e1) {
            String errorMessage = String.format(
                    "[MONITOR] [%s] [productName %s] [code %s] %s", category,
                    extractProductNameFromDto(dto), e1.getCode().getCode(),
                    e1.getLogMessage());
            failedProc = new FailedProcessingDto(processConfiguration.getHostname(),new Date(),errorMessage, message);              
            ackNegatively(reportingFactory, failedProc, message, errorMessage);            
            
        } catch (Exception e) {
            String errorMessage = String.format(
                    "[MONITOR] [%s] [productName %s] [code %s] [msg %s]",
                    category, extractProductNameFromDto(dto),
                    ErrorCode.INTERNAL_ERROR.getCode(), 
                    LogUtils.toString(e)
            );
            failedProc = new FailedProcessingDto(processConfiguration.getHostname(),new Date(),errorMessage, message);              
            ackNegatively(reportingFactory,failedProc, message, errorMessage);
            
        } finally {        	
            this.cleanProcessing(message);
        }

        if (appStatus.isFatalError()) {
            report.error("Fatal error");
            System.exit(-1);
        } else {
            appStatus.setWaiting(category);
        }        
        report.end("End metadata extraction");
    }

    /**
     * Acknowledge negatively the message processing and set app status in error
     * 
     * @param dto
     * @param errorMessage
     */
    final void ackNegatively(
    		final Reporting.Factory reportingFactory, 
            final FailedProcessingDto failedProc,
    		final GenericMessageDto<?> message,
            final String errorMessage) {
    	
        final Reporting reportAck = reportingFactory.newReporting(5);            
        reportAck.begin("Start acknowledging negatively");
        try {
            mqiService.ack(new AckMessageDto(message.getIdentifier(), Ack.ERROR, errorMessage, false), category);
            errorAppender.send(failedProc);            
            reportAck.end("End acknowledging negatively");
        } catch (AbstractCodedException ace) {
        	reportAck.error("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());     
        }
        appStatus.setError(category, "PROCESSING");
    }

    /**
     * Acknowledge positively the message processing
     * 
     * @param dto
     */
    final void ackPositively(final Reporting.Factory reportingFactory, final GenericMessageDto<T> message) {
    	
        final Reporting reportAck = reportingFactory.newReporting(5);            
        reportAck.begin("Start acknowledging positively");

        try {
            mqiService.ack(new AckMessageDto(message.getIdentifier(), Ack.OK, null, false), category);
            reportAck.end("End acknowledging positively");            
        } catch (AbstractCodedException ace) {            
        	reportAck.error("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());            
            appStatus.setError(category, "PROCESSING");
        }
    }

    /**
     * Extract product name from the DTO
     * 
     * @param dto
     * @return
     */
    protected abstract String extractProductNameFromDto(T dto);

    /**
     * Extract the metadata of a message
     * 
     * @param message
     * @return
     * @throws AbstractCodedException
     */
    protected abstract JSONObject extractMetadata(final Reporting.Factory reportingFactory, GenericMessageDto<T> message)
            throws AbstractCodedException;

    /**
     * Clean the working directory after extracting a metadata
     * 
     * @param message
     */
    protected abstract void cleanProcessing(GenericMessageDto<T> message);

    final File download(
    		final Reporting.Factory reportingFactory,
    		final ObsClient obsClient,
    		final ProductFamily family, 
    		final String productName,
    		final String keyObs
    ) 
    	throws AbstractCodedException
    {
        final Reporting reportDownload = reportingFactory
            	.product(family.toString(), productName)        
            	.newReporting(1);
            
        reportDownload.begin("Starting download of " + keyObs + " to local directory " + this.localDirectory);

		try {
			final File metadataFile = obsClient.downloadFile(family, keyObs, this.localDirectory);
			reportDownload.end("End download of " + keyObs);
			return metadataFile;
		} catch (AbstractCodedException e) {
			reportDownload.error("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			throw e;
		}         
    }
    
    final <E> E extractFromFilename(
			final Reporting.Factory reportingFactory,
			final ThrowingSupplier<E> supplier
	) 
		throws AbstractCodedException 
	{
    	return extractFrom(reportingFactory, 2, "filename", supplier);
	}
    
    final JSONObject extractFromFile(
			final Reporting.Factory reportingFactory,
			final ThrowingSupplier<JSONObject> supplier
	) 
		throws AbstractCodedException 
	{
    	return extractFrom(reportingFactory, 3, "file", supplier);
	}
    
	private final <E> E extractFrom(
			final Reporting.Factory reportingFactory,
			final int step,
			final String extraction,
			final ThrowingSupplier<E> supplier
	) 
		throws AbstractCodedException 
	{
		final Reporting reportExtractingFromFilename = reportingFactory.newReporting(step);
		reportExtractingFromFilename.begin("Start extraction from " + extraction);
		try {
			E res = supplier.get();
					//;
			reportExtractingFromFilename.end("End extraction from " + extraction);
			return res;
		} catch (AbstractCodedException e) {
			reportExtractingFromFilename.error("[code {}] {}", e.getCode().getCode(), e.getLogMessage());
			throw e;
		}
	

	}
}
