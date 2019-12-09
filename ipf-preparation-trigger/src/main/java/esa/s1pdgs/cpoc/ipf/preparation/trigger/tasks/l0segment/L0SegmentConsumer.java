package esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.l0segment;


import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.L0SegmentAppProperties;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.config.ProcessSettings;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.ipf.preparation.trigger.tasks.CatalogEventAdapter;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.client.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;

public class L0SegmentConsumer
        extends AbstractGenericConsumer<CatalogEvent> implements MqiListener<CatalogEvent> {
    
    
    private final Pattern blackList;
    
    /**
     * 
     */
    private String taskForFunctionalLog;
    
    private final long pollingIntervalMs;
    
    private final long pollingInitialDelayMs;

    /**
     * Constructor
     * 
     * @param jobsDispatcher
     * @param patternSettings
     * @param processSettings
     * @param mqiClient
     * @param appDataService
     * @param appStatus
     */
	public L0SegmentConsumer(
			final L0SegmentAppProperties appProperties, final ProcessSettings processSettings,
			final GenericMqiClient mqiClient, final StatusService mqiStatusService,
			final AppCatalogJobClient<CatalogEvent> appDataService, final ErrorRepoAppender errorRepoAppender,
			final AppStatus appStatus, final long pollingIntervalMs, final long pollingInitialDelayMs) {
		super(processSettings, mqiClient, mqiStatusService, appDataService, appStatus,
				errorRepoAppender, ProductCategory.LEVEL_SEGMENTS);
		this.blackList = (appProperties.getBlacklistPattern() == null) ? null : Pattern.compile(appProperties.getBlacklistPattern(), Pattern.CASE_INSENSITIVE);
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}

	@PostConstruct
	public void initService() {
		appStatus.setWaiting();
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<CatalogEvent>(mqiClient, category, this, pollingIntervalMs,
					pollingInitialDelayMs, esa.s1pdgs.cpoc.appstatus.AppStatus.NULL));
		}
	}
    
    
    @Override
    public void onMessage(final GenericMessageDto<CatalogEvent> mqiMessage) {
    	appStatus.setWaiting();
    	final Reporting.Factory reportingFactory = new LoggerReporting.Factory("L0_SEGMENTJobGeneration"); 
    	
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        final Reporting reporting = reportingFactory.newReporting(0);
        
        // process message
        appStatus.setProcessing(mqiMessage.getId());
        int step = 1;
        boolean ackOk = false;
        String errorMessage = "";
        String productName = mqiMessage.getBody().getKeyObjectStorage();
        
        if(skipProduct(productName)) {
        	LOGGER.warn("Skipping job generation for product {}", productName);
        	return;
        }
        
        FailedProcessingDto failedProc = new FailedProcessingDto();
        
        // Note: the report log of consume and global log is raised during
        // building job to get the datatake identifier which is the real
        // product name

        try {

            // Check if a job is already created for message identifier
            LOGGER.info(
                    "[MONITOR] [step 1] [productName {}] Creating/updating job",
                    productName);
            reporting.begin(
            		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getKeyObjectStorage())),            		
            		new ReportingMessage("Start job generation using {}", mqiMessage.getBody().getKeyObjectStorage())
            );
            AppDataJob<CatalogEvent> appDataJob = buildJob(mqiMessage);
            productName = appDataJob.getProduct().getProductName();

            // Dispatch job
            step++;
            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    productName);
            if (appDataJob.getState() == AppDataJobState.WAITING
                    || appDataJob
                            .getState() == AppDataJobState.DISPATCHING) {
                appDataJob.setState(AppDataJobState.DISPATCHING);
                appDataJob = appDataService.patchJob(appDataJob.getId(),
                        appDataJob, false, false, false);
                publish(appDataJob);
            } else {
                LOGGER.info(
                        "[MONITOR] [step 2] [productName {}] Job for datatake already dispatched",
                        productName);
            }

            // Ack
            step++;
            ackOk = true;

        } catch (final AbstractCodedException ace) {
            ackOk = false;
            errorMessage = String.format(
                    "[MONITOR] [step %d] [productName %s] [code %d] %s", step,
                    productName, ace.getCode().getCode(),
                    ace.getLogMessage());
            reporting.error(new ReportingMessage("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage()));

            failedProc = new FailedProcessingDto(processSettings.getHostname(),new Date(),errorMessage, mqiMessage);
        }

        // Ack and check if application shall stopped
        ackProcessing(mqiMessage, failedProc, ackOk, productName, errorMessage);

        LOGGER.info("[MONITOR] [step 0] [productName {}] End",
                productName);
        
        reporting.end(new ReportingMessage("End job generation using {}", mqiMessage.getBody().getKeyObjectStorage()));
    }

    private boolean skipProduct(final String productName) {
    	
    	boolean skip = false;
		if(blackList != null && blackList.matcher(productName).matches()) {
			skip = true;
		} 
		return skip;
	}

	protected AppDataJob<CatalogEvent> buildJob(
            final GenericMessageDto<CatalogEvent> mqiMessage)
            throws AbstractCodedException {
        final CatalogEvent catEvent = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        final List<AppDataJob<CatalogEvent>> existingJobs = appDataService.findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {        	
        	final CatalogEventAdapter eventAdapter = new CatalogEventAdapter(catEvent);

            // Search job for given datatake id
            final List<AppDataJob<CatalogEvent>> existingJobsForDatatake =
                    appDataService.findByProductDataTakeId(eventAdapter.datatakeId());

            if (CollectionUtils.isEmpty(existingJobsForDatatake)) {

                // Create the JOB
                final AppDataJob<CatalogEvent> jobDto = new AppDataJob<>();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                final AppDataJobProduct productDto = new AppDataJobProduct();
                productDto.setAcquisition(eventAdapter.swathType());
                productDto.setMissionId(eventAdapter.missionId());
                productDto.setDataTakeId(eventAdapter.datatakeId());
                productDto.setProductName("l0_segments_for_" + eventAdapter.datatakeId());
                productDto.setProcessMode(eventAdapter.processMode());
                productDto.setSatelliteId(eventAdapter.satelliteId());
                jobDto.setProduct(productDto);

                return appDataService.newJob(jobDto);
            } else {
				final AppDataJob<CatalogEvent> jobDto = existingJobsForDatatake.get(0);

                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                }
                jobDto.getMessages().add(mqiMessage);
                return appDataService.patchJob(jobDto.getId(), jobDto,
                        true, false, false);

            }

        } else {
            // Update pod if needed
			AppDataJob<CatalogEvent> jobDto = existingJobs.get(0);

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getId(), jobDto,
                        false, false, false);
            }
            // Job already exists
            return jobDto;
        }
    }

    @Override
    protected String getTaskForFunctionalLog() {
    	return this.taskForFunctionalLog;
    }
    
    @Override
    public void setTaskForFunctionalLog(final String taskForFunctionalLog) {
    	this.taskForFunctionalLog = taskForFunctionalLog; 
    }
    
}
