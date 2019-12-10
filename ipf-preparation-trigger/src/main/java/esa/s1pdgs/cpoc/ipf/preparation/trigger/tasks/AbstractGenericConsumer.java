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
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
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

    public AbstractGenericConsumer(
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final StatusService mqiStatusService,
            final AppCatalogJobClient<CatalogEvent> appDataService,
            final AppStatus appStatus,
            final ErrorRepoAppender errorRepoAppender,
            final ProductCategory category
    		) {
        this.processSettings = processSettings;
        this.mqiClient = mqiService;
        this.mqiStatusService = mqiStatusService;
        this.appDataService = appDataService;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
        this.category = category;
        this.blackList = (processSettings.getBlacklistPattern() == null) ? null : Pattern.compile(processSettings.getBlacklistPattern(), Pattern.CASE_INSENSITIVE);
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
        String productName = mqiMessage.getBody().getKeyObjectStorage();

        if (skipProduct(productName)) {
        	LOGGER.warn("Skipping job generation for product {}", productName);
        	return;
        }
        LOGGER.debug("Handling consumption of {} product {}", category, productName);

        try {
            // Check if a job is already created for message identifier
            LOGGER.info("Creating/updating job for product {}", productName);
            reporting.begin(
            		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getKeyObjectStorage())),            		
            		new ReportingMessage("Start job generation using {}", mqiMessage.getBody().getKeyObjectStorage())
            );
            AppDataJob<CatalogEvent> appDataJob = buildJob(mqiMessage);
            productName = appDataJob.getProduct().getProductName();

            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    productName);
            if (appDataJob.getState() == AppDataJobState.WAITING || appDataJob.getState() == AppDataJobState.DISPATCHING) {
                appDataJob.setState(AppDataJobState.DISPATCHING);
                appDataJob = appDataService.patchJob(appDataJob.getId(), appDataJob, false, false, false);
                publish(appDataJob, mqiMessage.getBody().getProductFamily(), mqiMessage.getInputKey());
            } else {
                LOGGER.info(
                        "[MONITOR] [step 2] [productName {}] Job for datatake already dispatched",
                        productName);
            }
            LOGGER.debug("Done handling consumption of {} product {}", category, productName);
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
	
	protected abstract AppDataJob<CatalogEvent> buildJob(final GenericMessageDto<CatalogEvent> mqiMessage);
    
    private final MqiConsumer<CatalogEvent> newMqiConsumer() {
    	return new MqiConsumer<CatalogEvent>(
    			mqiClient, 
    			category, 
    			this, 
    			processSettings.getFixedDelayMs(),
				processSettings.getInitialDelayMs(), 
				appStatus
		);
    }
    
    private boolean skipProduct(final String productName) {    	
    	boolean skip = false;
		if(blackList != null && blackList.matcher(productName).matches()) {
			skip = true;
		} 
		return skip;
	}
    
    
    
    protected void publish(
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
			mqiClient.publish(messageDto, ProductCategory.PREPARATION_JOB);
		} catch (final AbstractCodedException e) {
			throw new RuntimeException(
					String.format(
							"Error publishing %s message %s: %s", 
							ProductCategory.PREPARATION_JOB, 
							messageDto, 
							e.getLogMessage()
					),
					e
			);
		}
    }
}
