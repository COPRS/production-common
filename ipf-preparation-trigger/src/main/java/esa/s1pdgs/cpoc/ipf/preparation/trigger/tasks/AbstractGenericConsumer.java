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
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
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
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

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
    	final Reporting.Factory reportingFactory = new LoggerReporting.Factory("L0_SEGMENTJobGeneration"); 
        final Reporting reporting = reportingFactory.newReporting(0);
        final String productName = mqiMessage.getBody().getKeyObjectStorage();
        final ProductFamily family = mqiMessage.getBody().getProductFamily();


        try {
            // TODO generalize filtering
            if (skipProduct(productName)) {
            	LOGGER.warn("Skipping job generation for product {}", productName);
            	return;
            }
            
            // S1PRO-483: check for matching products if they are over sea. If not, simply skip the
            // production
            if (seaCoverageCheckPattern.matcher(productName).matches()) {          	
            	if (metadataClient.getSeaCoverage(family, productName) <= processSettings.getMinSeaCoveragePercentage()) {
                    return;
                }
            }  
            LOGGER.debug("Handling consumption of {} product {}", category, productName);
        	
            // Check if a job is already created for message identifier
            LOGGER.info("Creating/updating job for product {}", productName);
            reporting.begin(
            		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getKeyObjectStorage())),            		
            		new ReportingMessage("Start job generation using {}", mqiMessage.getBody().getKeyObjectStorage())
            );
            final AppDataJob<CatalogEvent> appDataJob = dispatch(mqiMessage);
            publish(appDataJob, mqiMessage.getBody().getProductFamily(), mqiMessage.getInputKey());
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
    	job.setAppDataJob(appDataJob);
    	
    	final GenericPublicationMessageDto<IpfPreparationJob> messageDto = new GenericPublicationMessageDto<IpfPreparationJob>(
    			appDataJob.getId(), 
    			family, 
    			job
    	);
    	messageDto.setInputKey(topic);
    	messageDto.setOutputKey(job.getProductFamily().name());
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
    
    private final boolean skipProduct(final String productName) {    	
    	boolean skip = false;
		if(blackList != null && blackList.matcher(productName).matches()) {
			skip = true;
		} 
		return skip;
	}
}
