package esa.s1pdgs.cpoc.production.trigger.tasks;

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
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.metadata.client.MetadataClient;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.queue.IpfPreparationJob;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericPublicationMessageDto;
import esa.s1pdgs.cpoc.production.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.production.trigger.report.DispatchReportInput;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFactory;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public abstract class AbstractGenericConsumer<T extends AbstractMessage> implements MqiListener<CatalogEvent> {
    protected static final Logger LOGGER = LogManager.getLogger(AbstractGenericConsumer.class);
    
    protected final ProcessSettings processSettings;
    protected final AppCatalogJobClient<CatalogEvent> appDataService;    
    private final GenericMqiClient mqiClient;
    private final AppStatus appStatus;    
    private final ErrorRepoAppender errorRepoAppender;    
     private final Pattern blackList;    
    private final Pattern seaCoverageCheckPattern;    
    private final MetadataClient metadataClient;

    public AbstractGenericConsumer(
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final AppCatalogJobClient<CatalogEvent> appDataService,
            final AppStatus appStatus,
            final ErrorRepoAppender errorRepoAppender,
            final MetadataClient metadataClient
    ) {
        this.processSettings = processSettings;
        this.mqiClient = mqiService;
        this.appDataService = appDataService;
        this.appStatus = appStatus;
        this.errorRepoAppender = errorRepoAppender;
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
    public final void onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) throws Exception {
        final CatalogEvent event = mqiMessage.getBody();
        final String productName = event.getProductName();
        
        final Reporting reporting = ReportingUtils.newReportingBuilder()
        		.predecessor(event.getUid())
        		.newReporting("ProductionTrigger");

        reporting.begin(
        		new FilenameReportingInput(productName),
        		new ReportingMessage("Received CatalogEvent for %s", productName)
        );
        try {
            if (shallBeSkipped(event, reporting)) {
            	return;
            }
            LOGGER.debug("Handling consumption of product {}", productName);

            final AppDataJob<CatalogEvent> appDataJob = dispatch(mqiMessage, reporting);
            
        	final IpfPreparationJob job = new IpfPreparationJob();
        	job.setProductFamily(event.getProductFamily());
        	job.setAppDataJob(appDataJob);
        	job.setUid(reporting.getUid());
        	
        	final GenericPublicationMessageDto<IpfPreparationJob> messageDto = new GenericPublicationMessageDto<IpfPreparationJob>(
        			appDataJob.getId(), 
        			event.getProductFamily(), 
        			job
        	);
        	messageDto.setInputKey(mqiMessage.getInputKey());
        	messageDto.setOutputKey(event.getProductFamily().name());
        	mqiClient.publish(messageDto, ProductCategory.PREPARATION_JOBS);  
        	        	
            reporting.end(new ReportingMessage("IpfPreparationJob for product %s created", productName));            
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
	
	protected final AppDataJob<CatalogEvent> patchJob(
			final AppDataJob<CatalogEvent> appDataJob,
			final String productName,
			final String type,
			final ReportingFactory reportingFactory
	) throws AbstractCodedException {
        final Reporting reporting = reportingFactory.newReporting("Dispatch");
        
        reporting.begin(
        		new DispatchReportInput(appDataJob.getId(), productName, type),
        		new ReportingMessage("Dispatching AppDataJob %s for %s %s", appDataJob.getId(), type, productName)
        );     
        try {
			final AppDataJob<CatalogEvent> retVal = appDataService.patchJob(appDataJob.getId(), appDataJob, false,false, false);
			reporting.end(
					new ReportingMessage("AppDataJob %s for %s %s dispatched", appDataJob.getId(), type, productName)
			);
			return retVal;
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

	protected abstract AppDataJob<CatalogEvent> dispatch(
			final GenericMessageDto<CatalogEvent> mqiMessage,
			final ReportingFactory reportingFactory
	) 
		throws AbstractCodedException;

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
				seaReport.begin(new FilenameReportingInput(productName), new ReportingMessage("Checking sea coverage"));				
				if (metadataClient.getSeaCoverage(family, productName) <= processSettings.getMinSeaCoveragePercentage()) {
					seaReport.end(new ReportingMessage("Product %s is not over sea", productName));
					reporting.end(new ReportingMessage("Product %s is not over sea, skipping", productName));
					LOGGER.warn("Skipping job generation for product {} because it is not over sea", productName);
			        return true;
			    }
				else {
					seaReport.end(new ReportingMessage("Product %s is over sea", productName));
				}
			}
		} catch (final Exception e) {
			seaReport.error(new ReportingMessage("SeaCoverage check failed: %s", LogUtils.toString(e)));
			throw e;			
		}        
        return false;
	}    
}
