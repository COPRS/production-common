package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;


import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJob;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobProduct;
import esa.s1pdgs.cpoc.appcatalog.server.job.db.AppDataJobState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.jobgenerator.config.L0SegmentAppProperties;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.MqiConsumer;
import esa.s1pdgs.cpoc.mqi.MqiListener;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.status.AppStatus;

public class L0SegmentAppConsumer
        extends AbstractGenericConsumer<ProductDto> implements MqiListener<ProductDto> {

    /**
     * Pattern built from the regular expression given in configuration
     */
    private final Pattern pattern;
    
    /**
     * 
     */
    private final Map<String, Integer> patternGroups;
    
    
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
	public L0SegmentAppConsumer(final AbstractJobsDispatcher<ProductDto> jobsDispatcher,
			final L0SegmentAppProperties appProperties, final ProcessSettings processSettings,
			final GenericMqiClient mqiClient, final StatusService mqiStatusService,
			final AppCatalogJobClient<ProductDto> appDataService, final ErrorRepoAppender errorRepoAppender,
			final AppStatus appStatus, final long pollingIntervalMs, final long pollingInitialDelayMs) {
		super(jobsDispatcher, processSettings, mqiClient, mqiStatusService, appDataService, appStatus,
				errorRepoAppender, ProductCategory.LEVEL_SEGMENTS);
		this.pattern = Pattern.compile(appProperties.getNameRegexpPattern(), Pattern.CASE_INSENSITIVE);
		this.patternGroups = appProperties.getNameRegexpGroups();
		this.blackList = (appProperties.getBlacklistPattern() == null) ? null : Pattern.compile(appProperties.getBlacklistPattern(), Pattern.CASE_INSENSITIVE);
		this.pollingIntervalMs = pollingIntervalMs;
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}

	@PostConstruct
	public void initService() {
		appStatus.setWaiting();
		if (pollingIntervalMs > 0) {
			final ExecutorService service = Executors.newFixedThreadPool(1);
			service.execute(new MqiConsumer<ProductDto>(mqiClient, category, this, pollingIntervalMs,
					pollingInitialDelayMs, esa.s1pdgs.cpoc.status.AppStatus.NULL));
		}
	}
    
    
    @Override
    public void onMessage(GenericMessageDto<ProductDto> mqiMessage) {
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
        String productName = mqiMessage.getBody().getProductName();
        
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
            		new FilenameReportingInput(Collections.singletonList(mqiMessage.getBody().getProductName())),            		
            		new ReportingMessage("Start job generation using {}", mqiMessage.getBody().getProductName())
            );
            AppDataJob<ProductDto> appDataJob = buildJob(mqiMessage);
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
                jobsDispatcher.dispatch(appDataJob);
            } else {
                LOGGER.info(
                        "[MONITOR] [step 2] [productName {}] Job for datatake already dispatched",
                        productName);
            }

            // Ack
            step++;
            ackOk = true;

        } catch (AbstractCodedException ace) {
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
        
        reporting.end(new ReportingMessage("End job generation using {}", mqiMessage.getBody().getProductName()));
    }

    private boolean skipProduct(String productName) {
    	
    	boolean skip = false;
		if(blackList != null && blackList.matcher(productName).matches()) {
			skip = true;
		} 
		return skip;
	}

	protected AppDataJob<ProductDto> buildJob(
            GenericMessageDto<ProductDto> mqiMessage)
            throws AbstractCodedException {
        ProductDto leveldto = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        List<AppDataJob<ProductDto>> existingJobs = appDataService.findByMessagesId(mqiMessage.getId());

        if (CollectionUtils.isEmpty(existingJobs)) {

            // Extract information from name
            Matcher m = pattern.matcher(leveldto.getProductName());
            if (!m.matches()) {
                throw new InvalidFormatProduct(
                        "Don't match with regular expression "
                                + this.pattern.pattern());
            }
            String satelliteId = m.group(this.patternGroups.get("satelliteId"));
            String missionId = m.group(this.patternGroups.get("missionId"));
            String acquisition = m.group(this.patternGroups.get("acquisition"));
            String datatakeID = m.group(this.patternGroups.get("datatakeId"));

            // Search job for given datatake id
            List<AppDataJob<ProductDto>> existingJobsForDatatake =
                    appDataService.findByProductDataTakeId(datatakeID);

            if (CollectionUtils.isEmpty(existingJobsForDatatake)) {

                // Create the JOB
                AppDataJob<ProductDto> jobDto = new AppDataJob<>();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                AppDataJobProduct productDto = new AppDataJobProduct();
                productDto.setAcquisition(acquisition);
                productDto.setMissionId(missionId);
                productDto.setDataTakeId(datatakeID);
                productDto.setProductName("l0_segments_for_" + datatakeID);
                productDto.setProcessMode(leveldto.getMode());
                productDto.setSatelliteId(satelliteId);
                jobDto.setProduct(productDto);

                return appDataService.newJob(jobDto);
            } else {
				AppDataJob<ProductDto> jobDto = (AppDataJob<ProductDto>) existingJobsForDatatake.get(0);

                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                }
                jobDto.getMessages().add(mqiMessage);
                return appDataService.patchJob(jobDto.getId(), jobDto,
                        true, false, false);

            }

        } else {
            // Update pod if needed
			AppDataJob<ProductDto> jobDto = (AppDataJob<ProductDto>) existingJobs.get(0);

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
    public void setTaskForFunctionalLog(String taskForFunctionalLog) {
    	this.taskForFunctionalLog = taskForFunctionalLog; 
    }
    
}
