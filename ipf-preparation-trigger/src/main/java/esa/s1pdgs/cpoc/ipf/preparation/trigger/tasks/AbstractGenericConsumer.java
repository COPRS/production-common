package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataQueryException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public abstract class AbstractGenericConsumer<T extends AbstractMessage> implements MqiListener<CatalogEvent> {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractGenericConsumer.class);
    
    protected final ProcessSettings processSettings;
    protected final GenericMqiClient mqiClient;
    protected final AppCatalogJobClient<CatalogEvent> appDataService;
    protected final StatusService mqiStatusService;
    protected final AppStatus appStatus;    
    protected final ErrorRepoAppender errorRepoAppender;    
    protected final ProductCategory category;
    private final Pattern blackList;    
    private final Pattern seaCoverageCheckPattern;    
    private final MetadataClient metadataClient;

    public AbstractGenericConsumer(
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final StatusService mqiStatusService,
            final AppCatalogJobClient<CatalogEvent> appDataService,
            final AppStatus appStatus,
            final ErrorRepoAppender errorRepoAppender,
            final ProductCategory category,
            final MetadataClient metadataClient
    ) {
        this.processSettings = processSettings;
        this.mqiClient = mqiService;
        this.mqiStatusService = mqiStatusService;
        this.appDataService = appDataService;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
        this.category = category;
        this.blackList = (processSettings.getBlacklistPattern() == null) ? 
        		null : 
        		Pattern.compile(processSettings.getBlacklistPattern(), Pattern.CASE_INSENSITIVE);
		this.seaCoverageCheckPattern = Pattern.compile(processSettings.getSeaCoverageCheckPattern());
		this.metadataClient = metadataClient;
    }
    
	@PostConstruct
	public void initService() {
		appStatus.setWaiting();
		if (processSettings.getFixedDelayMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(newMqiConsumer());
		}
	} 

    @Override
    public void onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) {
    	final Reporting reporting = ReportingUtils.newReportingBuilderFor("L0_SEGMENTJobGeneration")
    			.newReporting();

        final CatalogEvent event = mqiMessage.getBody();
        final String productName = event.getProductName();

        try {
            if (shallBeSkipped(event)) {
            	return;
            }
            LOGGER.debug("Handling consumption of {} product {}", category, productName);
        	
            // Check if a job is already created for message identifier
            LOGGER.info("Creating/updating job for product {}", productName);
            reporting.begin(
            		new FilenameReportingInput(Collections.singletonList(event.getKeyObjectStorage())),            		
            		new ReportingMessage("Start job generation using {}", event.getKeyObjectStorage())
            );
            final AppDataJob<CatalogEvent> appDataJob = dispatch(mqiMessage);
            publish(appDataJob, event.getProductFamily(), mqiMessage.getInputKey());
            LOGGER.debug("Done handling consumption of {} product {}", category, productName);
            reporting.end(new ReportingMessage("End job generation using {}", productName));
        } catch (final AbstractCodedException ace) {            
            final String errorMessage = String.format(
            		"[productName %s] [code %d] %s",
            		productName, 
            		ace.getCode().getCode(),
            		ace.getLogMessage()
            );
            reporting.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));
            errorRepoAppender.send(
            	new FailedProcessingDto(processSettings.getHostname(),new Date(), errorMessage, mqiMessage)
            );
        }
    }
    
	protected abstract AppDataJob<CatalogEvent> dispatch(final GenericMessageDto<CatalogEvent> mqiMessage) throws AbstractCodedException;
	    
    private final void publish(
    		final AppDataJob<CatalogEvent> appDataJob,
    		final ProductFamily family,
    		final String topic    		
    ) {   
    	final IpfPreparationJob job = new IpfPreparationJob();
    	job.setProductFamily(family);
    	job.setAppDataJob(appDataJob);
    	
    	final GenericPublicationMessageDto<IpfPreparationJob> messageDto = new GenericPublicationMessageDto<IpfPreparationJob>(
    			appDataJob.getId(), 
    			family, 
    			job
    	);
    	messageDto.setInputKey(topic);
    	messageDto.setOutputKey(family.name());
		try {
			mqiClient.publish(messageDto, ProductCategory.PREPARATION_JOBS);
		} catch (final AbstractCodedException e) {
			throw new RuntimeException(
					String.format(
							"Error publishing %s message %s: %s", 
							ProductCategory.PREPARATION_JOBS, 
							messageDto, 
							e.getLogMessage()
					),
					e
			);
		}
    }

    private final MqiConsumer<CatalogEvent> newMqiConsumer() {
    	return new MqiConsumer<CatalogEvent>(
    			mqiClient, 
    			ProductCategory.CATALOG_EVENT, 
    			this, 
    			processSettings.getFixedDelayMs(),
				processSettings.getInitialDelayMs(), 
				appStatus
		);
    }

	private final boolean shallBeSkipped(final CatalogEvent event) throws MetadataQueryException {
        final String productName = event.getProductName();
        final ProductFamily family = event.getProductFamily();
        final String productType = event.getProductType();
        
		if (blackList != null && blackList.matcher(productName).matches()) {
			LOGGER.warn("Skipping job generation for product {} due to blacklist {}", productName, blackList);
			return true;
		}
		
		// Only sessions are used
		if (family == ProductFamily.EDRS_SESSION 
				&& 	EdrsSessionFileType.valueOf(productType.toUpperCase()) == EdrsSessionFileType.RAW) {
			LOGGER.warn("Skipping job generation for product {} because it's EdrsSessionFileType.RAW", productName);
			return true;
		}
		
        // S1PRO-483: check for matching products if they are over sea. If not, simply skip the
        // production
        if (seaCoverageCheckPattern.matcher(productName).matches()) {          	
        	if (metadataClient.getSeaCoverage(family, productName) <= processSettings.getMinSeaCoveragePercentage()) {
        		LOGGER.warn("Skipping job generation for product {} because it is not over sea", productName);
                return true;
            }
        }        
        return false;
	}    
}
