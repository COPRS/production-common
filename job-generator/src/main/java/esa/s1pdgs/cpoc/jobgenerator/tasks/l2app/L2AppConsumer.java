package esa.s1pdgs.cpoc.jobgenerator.tasks.l2app;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AppCatalogJobClient;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.errorrepo.ErrorRepoAppender;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;
import esa.s1pdgs.cpoc.jobgenerator.config.L0SlicePatternSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiClient;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.report.LoggerReporting;
import esa.s1pdgs.cpoc.report.Reporting;

@Component
@ConditionalOnProperty(name = "process.level", havingValue = "L2")
public class L2AppConsumer extends AbstractGenericConsumer<ProductDto> {

    /**
     * Settings used to extract information from L0 product name
     */
    private final L0SlicePatternSettings patternSettings;

    /**
     * Pattern built from the regular expression given in configuration
     */
    private final Pattern l2SLicesPattern;

    /**
     * Constructor
     * 
     * @param jobsDispatcher
     * @param patternSettings
     * @param processSettings
     * @param mqiService
     * @param appDataService
     * @param appStatus
     */
    @Autowired
    public L2AppConsumer(
            final AbstractJobsDispatcher<ProductDto> jobsDispatcher,
            final L0SlicePatternSettings patternSettings,
            final ProcessSettings processSettings,
            final GenericMqiClient mqiService,
            final StatusService mqiStatusService,
            @Qualifier("appCatalogServiceForLevelProducts") final AppCatalogJobClient appDataService,
            final ErrorRepoAppender errorRepoAppender,
            final AppStatus appStatus) {
        super(jobsDispatcher, processSettings, mqiService, mqiStatusService,
                appDataService, appStatus, errorRepoAppender, ProductCategory.LEVEL_PRODUCTS);
        this.patternSettings = patternSettings;
        this.l2SLicesPattern = Pattern.compile(this.patternSettings.getRegexp(),
                Pattern.CASE_INSENSITIVE);
    }

    /**
     * Periodic function for processing messages
     */
    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}", initialDelayString = "${process.initial-delay-ms}")
    public void consumeMessages() {
    	final Reporting.Factory reportingFactory = new LoggerReporting.Factory(LOGGER, "L2JobGeneration"); 
    	final Reporting reporting = reportingFactory.newReporting(0);
    	
        // First, consume message
        GenericMessageDto<ProductDto> mqiMessage = readMessage();
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        // process message
        appStatus.setProcessing(mqiMessage.getIdentifier());
        int step = 1;
        boolean ackOk = false;
        String errorMessage = "";
        String productName = mqiMessage.getBody().getProductName();
        
        FailedProcessingDto failedProc =  new FailedProcessingDto();
        
        try {

            // Check if a job is already created for message identifier
            LOGGER.info("[MONITOR] [step 1] [productName {}] Creating job",
                    productName);
            reporting.reportStart("Start job generation using " + mqiMessage.getBody().getProductName());
            AppDataJobDto appDataJob = buildJob(mqiMessage);
            productName = appDataJob.getProduct().getProductName();

            // Dispatch job
            step++;
            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    productName);
            if (appDataJob.getState() == AppDataJobDtoState.WAITING) {
                appDataJob.setState(AppDataJobDtoState.DISPATCHING);
                appDataJob = appDataService.patchJob(appDataJob.getIdentifier(),
                        appDataJob, false, false, false);
            }
            jobsDispatcher.dispatch(appDataJob);

            // Ack
            step++;
            ackOk = true;

        } catch (AbstractCodedException ace) {
            ackOk = false;
            errorMessage = String.format(
                    "[MONITOR] [step %d] [productName %s] [code %d] %s", step,
                    productName, ace.getCode().getCode(), ace.getLogMessage());
            reporting.reportError("[code {}] {}", ace.getCode().getCode(), ace.getLogMessage());            
            failedProc = new FailedProcessingDto(processSettings.getHostname(),new Date(),errorMessage, mqiMessage);  
        }

        // Ack and check if application shall stopped
        ackProcessing(mqiMessage, failedProc, ackOk, productName, errorMessage);

        LOGGER.info("[MONITOR] [step 0] [productName {}] End", productName);
        reporting.reportStop("End job generation using " + mqiMessage.getBody().getProductName());
    }

    protected AppDataJobDto buildJob(GenericMessageDto<ProductDto> mqiMessage)
            throws AbstractCodedException {
        ProductDto leveldto = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        List<AppDataJobDto> existingJobs = appDataService
                .findByMessagesIdentifier(mqiMessage.getIdentifier());

        if (CollectionUtils.isEmpty(existingJobs)) {
            // Job does not exists => create it
            Matcher m = l2SLicesPattern.matcher(leveldto.getProductName());
            if (!m.matches()) {
                throw new InvalidFormatProduct(
                        "Don't match with regular expression "
                                + this.patternSettings.getRegexp());
            }
            String satelliteId = m.group(this.patternSettings.getMGroupSatId());
            String missionId =
                    m.group(this.patternSettings.getMGroupMissionId());
            String acquisition =
                    m.group(this.patternSettings.getMGroupAcquisition());
            String startTime =
                    m.group(this.patternSettings.getMGroupStartTime());
            String stopTime = m.group(this.patternSettings.getMGroupStopTime());

            // Create the JOB
            AppDataJobDto jobDto = new AppDataJobDto();
            // General details
            jobDto.setLevel(processSettings.getLevel());
            jobDto.setPod(processSettings.getHostname());
            // Messages
            jobDto.getMessages().add(mqiMessage);
            // Product
            AppDataJobProductDto productDto = new AppDataJobProductDto();
            productDto.setAcquisition(acquisition);
            productDto.setMissionId(missionId);
            productDto.setProductName(leveldto.getProductName());
            productDto.setProcessMode(leveldto.getMode());
            productDto.setSatelliteId(satelliteId);
            productDto.setStartTime(DateUtils.convertToAnotherFormat(startTime,
                    L0SlicePatternSettings.TIME_FORMATTER,
                    AppDataJobProductDto.TIME_FORMATTER));
            productDto.setStopTime(DateUtils.convertToAnotherFormat(stopTime,
                    L0SlicePatternSettings.TIME_FORMATTER,
                    AppDataJobProductDto.TIME_FORMATTER));
            jobDto.setProduct(productDto);

            return appDataService.newJob(jobDto);

        } else {
            // Update pod if needed
            AppDataJobDto jobDto = existingJobs.get(0);

            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getIdentifier(), jobDto,
                        false, false, false);
            }
            // Job already exists
            return jobDto;
        }
    }

    @Override
    protected String getTaskForFunctionalLog() {
        return "L2JobGeneration";
    }
}
