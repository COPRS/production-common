package esa.s1pdgs.cpoc.production.trigger.service;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.AppDataJobState;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.production.trigger.appcat.AppCatAdapter;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.production.trigger.consumption.CatalogEventAdapter;
import esa.s1pdgs.cpoc.production.trigger.consumption.ProductTypeConsumptionHandler;
import esa.s1pdgs.cpoc.production.trigger.report.DispatchReportInput;
import esa.s1pdgs.cpoc.production.trigger.report.SeaCoverageCheckReportingOutput;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public class GenericConsumer implements MqiListener<CatalogEvent> {
    static final Logger LOGGER = LogManager.getLogger(GenericConsumer.class);
    
    private final ProcessSettings processSettings;
    private final AppCatAdapter appCat;
    private final GenericMqiClient mqiClient;
    private final AppStatus appStatus;    
    private final ErrorRepoAppender errorRepoAppender;    
    private final Pattern blackList;    
    private final Pattern seaCoverageCheckPattern;    
    private final MetadataClient metadataClient;
    private final ProductTypeConsumptionHandler consumptionHandler;
    
    public GenericConsumer(
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final AppCatAdapter appCat,
            final AppStatus appStatus,
            final ErrorRepoAppender errorRepoAppender,
            final MetadataClient metadataClient,
            final ProductTypeConsumptionHandler consumptionHandler
    ) {
        this.processSettings = processSettings;
        this.mqiClient = mqiService;
        this.appCat = appCat;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
        this.blackList = (processSettings.getBlacklistPattern() == null) ? 
        		null : 
        		Pattern.compile(processSettings.getBlacklistPattern(), Pattern.CASE_INSENSITIVE);
		this.seaCoverageCheckPattern = Pattern.compile(processSettings.getSeaCoverageCheckPattern());
		this.metadataClient = metadataClient;
		this.consumptionHandler = consumptionHandler;
    }

	@PostConstruct
	public void initService() {
		appStatus.setWaiting();
		if (processSettings.getFixedDelayMs() > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<CatalogEvent>(
	    			mqiClient, 
	    			ProductCategory.CATALOG_EVENT, 
	    			this, 
	    			processSettings.getFixedDelayMs(),
					processSettings.getInitialDelayMs(), 
					appStatus
			));
		}
	} 

    @Override
    public final void onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) throws Exception {
        final CatalogEvent event = mqiMessage.getBody();
        final String productName = event.getProductName();
        
        final Reporting reporting = ReportingUtils.newReportingBuilder()
        		.predecessor(event.getUid())
        		.newReporting("ProductionTrigger");
        
        LOGGER.debug("Handling consumption of product {}", productName);
        try {
            reporting.begin(
            		ReportingUtils.newFilenameReportingInputFor(event.getProductFamily(), productName),
            		new ReportingMessage("Received CatalogEvent for %s", productName)
            );  
            if (!shallBeSkipped(event, reporting)) {                   
                final AppDataJob appDataJob = buildJob(mqiMessage, productName);        
                LOGGER.trace("== appDataJob(1) {}", appDataJob.toString());
            	
            	if (consumptionHandler.isReady(appDataJob, productName)) {
                    LOGGER.info("Dispatching {} product {}", consumptionHandler.type(), productName);
                    dispatch(mqiMessage, appDataJob, productName, consumptionHandler.type(), reporting);
                    reporting.end(new ReportingMessage("IpfPreparationJob for product %s created", productName));  
                    return;
            	}    
            	else {
            		LOGGER.debug("Job {} for {} not ready yet",appDataJob.getId(), productName);
            	}
            }
            else {
            	LOGGER.debug("CatalogEvent for {} is ignored", productName);
            }
            reporting.end(new ReportingMessage("No action for CatalogEvent for product %s", productName)); 
            LOGGER.debug("Done handling consumption of product {}", productName);
        } catch (final Exception e) {        	
        	reporting.error(new ReportingMessage("Error on handling CatalogEvent: %s", LogUtils.toString(e)));
        	throw e;
        }
    }       

	@Override
	public final void onTerminalError(final GenericMessageDto<CatalogEvent> message, final Exception error) {
        LOGGER.error(error);
        errorRepoAppender.send(
        	new FailedProcessingDto(processSettings.getHostname(), new Date(), error.getMessage(), message)
        );
	}
	
	private final AppDataJob buildJob(final GenericMessageDto<CatalogEvent> mqiMessage, final String productName) 
			throws AbstractCodedException {
		final Optional<AppDataJob> result = appCat.findJobFor(mqiMessage); 
		
		// there is already a job for this message --> possible restart scenario --> just update the pod name 
		if (result.isPresent()) {		
			final AppDataJob job = result.get();
			LOGGER.debug("Found job {} already associated to mqiMessage {}. Ignoring new message ...",
					job.getId(), mqiMessage.getId());
			return job;  		
		}
		
    	final CatalogEventAdapter eventAdapter = CatalogEventAdapter.of(mqiMessage);
    	final Optional<AppDataJob> specificJob = consumptionHandler.findAssociatedJobFor(appCat, eventAdapter);
    	
    	// no job yet associated to this message --> create job and persist
    	if (!specificJob.isPresent()) {
            final AppDataJob job = newJobFor(mqiMessage);
            
            if (LOGGER.isTraceEnabled()) {
            	LOGGER.trace("Created new {} for {}", job, mqiMessage);
            }
            else {
            	LOGGER.debug("Created new appDataJob {} for mqiMessage {}", job.getId(), mqiMessage.getId());
            }
            return job;
    	}  	
    	return specificJob.get();
	}
	
	private final void dispatch(
			final GenericMessageDto<CatalogEvent> mqiMessage,
			final AppDataJob appDataJob,
			final String productName,
			final String type,
			final ReportingFactory reportingFactory
	) throws AbstractCodedException {
        final CatalogEvent event = mqiMessage.getBody();
		
        final Reporting reporting = reportingFactory.newReporting("Dispatch");
        
        reporting.begin(
        		DispatchReportInput.newInstance(appDataJob.getId(), productName, type),
        		new ReportingMessage("Dispatching AppDataJob %s for %s %s", appDataJob.getId(), type, productName)
        );     
        try {
            appDataJob.setState(AppDataJobState.DISPATCHING);            
			final AppDataJob dispatchedJob = appCat.update(appDataJob);
        	final IpfPreparationJob job = new IpfPreparationJob();
        	job.setProductFamily(event.getProductFamily());
        	job.setAppDataJob(dispatchedJob);
        	job.setUid(reporting.getUid());
        	
        	final GenericPublicationMessageDto<IpfPreparationJob> messageDto = new GenericPublicationMessageDto<IpfPreparationJob>(
        			dispatchedJob.getId(), 
        			event.getProductFamily(), 
        			job
        	);
        	messageDto.setInputKey(mqiMessage.getInputKey());
        	messageDto.setOutputKey(event.getProductFamily().name());
        	mqiClient.publish(messageDto, ProductCategory.PREPARATION_JOBS);  
			reporting.end(
					new ReportingMessage("AppDataJob %s for %s %s dispatched", appDataJob.getId(), type, productName)
			);
		} catch (final AbstractCodedException e) {
			reporting.error(new ReportingMessage(
					"Error on dispatching AppDataJob %s for %s %s: %s", 
					appDataJob.getId(), 
					type, 
					productName,
					LogUtils.toString(e)
			));
			throw e;
		}
	}
	
    
	private final AppDataJob newJobFor(final GenericMessageDto<CatalogEvent> mqiMessage) throws AbstractCodedException {
        final AppDataJob job = new AppDataJob();
        job.setLevel(processSettings.getLevel());
        job.setPod(processSettings.getHostname());
        job.getMessages().add(mqiMessage);
        job.setProduct(consumptionHandler.newProductFor(mqiMessage));
        return appCat.create(job);
	}

	private final boolean shallBeSkipped(final CatalogEvent event, final Reporting reporting) throws Exception {
        final String productName = event.getProductName();
        final ProductFamily family = event.getProductFamily();
        final String productType = event.getProductType();
        
		if (blackList != null && blackList.matcher(productName).matches()) {
			reporting.end(new ReportingMessage("Product %s matches blacklist, skipping", productName));
			LOGGER.warn("Skipping job generation for product {} due to blacklist {}", productName, blackList);
			return true;
		}
		
		// Only sessions are used
		if (family == ProductFamily.EDRS_SESSION 
				&& 	EdrsSessionFileType.valueOf(productType.toUpperCase()) == EdrsSessionFileType.RAW) {
			reporting.end(new ReportingMessage("Product %s is RAW, skipping", productName));
			LOGGER.warn("Skipping job generation for product {} because it's EdrsSessionFileType.RAW", productName);
			return true;
		}
		
        // S1PRO-483: check for matching products if they are over sea. If not, simply skip the
        // production
		final Reporting seaReport = reporting.newReporting("SeaCoverageCheck");
        try {
			if (seaCoverageCheckPattern.matcher(productName).matches()) {   
				seaReport.begin(
						ReportingUtils.newFilenameReportingInputFor(family, productName),
						new ReportingMessage("Checking sea coverage")
				);	
				if (metadataClient.getSeaCoverage(family, productName) <= processSettings.getMinSeaCoveragePercentage()) {
					seaReport.end(
							new SeaCoverageCheckReportingOutput(false), 
							new ReportingMessage("Product %s is not over sea", productName)
					);
					
					reporting.end(new ReportingMessage("Product %s is not over sea, skipping", productName));
					LOGGER.warn("Skipping job generation for product {} because it is not over sea", productName);
			        return true;
			    }
				else {
					seaReport.end(
							new SeaCoverageCheckReportingOutput(true), 
							new ReportingMessage("Product %s is over sea", productName)
					);
				}
			}
		} catch (final Exception e) {
			seaReport.error(new ReportingMessage("SeaCoverage check failed: %s", LogUtils.toString(e)));
			throw e;			
		}        
        return false;
	}    
}
