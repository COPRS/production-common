package esa.s1pdgs.cpoc.jobgenerator.tasks.l1app;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.jobgenerator.config.L0SlicePatternSettings;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Component
@ConditionalOnProperty(name = "process.level", havingValue = "L1")

public class L1AppConsumer extends AbstractGenericConsumer<LevelProductDto> {

    /**
     * Settings used to extract information from L0 product name
     */
    private final L0SlicePatternSettings patternSettings;

    /**
     * Pattern built from the regular expression given in configuration
     */
    private final Pattern l0SLicesPattern;

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
    public L1AppConsumer(
            final AbstractJobsDispatcher<LevelProductDto> jobsDispatcher,
            final L0SlicePatternSettings patternSettings,
            final ProcessSettings processSettings,
            @Qualifier("mqiServiceForLevelProducts") final GenericMqiService<LevelProductDto> mqiService,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService,
            @Qualifier("appCatalogServiceForLevelProducts") final AbstractAppCatalogJobService<LevelProductDto> appDataService,
            final AppStatus appStatus) {
        super(jobsDispatcher, processSettings, mqiService, mqiStatusService,
                appDataService, appStatus);
        this.patternSettings = patternSettings;
        this.l0SLicesPattern = Pattern.compile(this.patternSettings.getRegexp(),
                Pattern.CASE_INSENSITIVE);
    }

    /**
     * Periodic function for processing messages
     */
    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}", initialDelayString = "${process.initial-delay-ms}")
    public void consumeMessages() {
        // First, consume message
        GenericMessageDto<LevelProductDto> mqiMessage = readMessage();
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        // process message
        appStatus.setProcessing(mqiMessage.getIdentifier());
        LOGGER.info(
                "[REPORT] [MONITOR] [step 0] [s1pdgsTask L1JobGeneration] [subTask Consume] [START] [productName {}] Starting job generation",
                getProductName(mqiMessage));
        int step = 1;
        boolean ackOk = false;
        String errorMessage = "";

        try {

            // Check if a job is already created for message identifier
            LOGGER.info("[MONITOR] [step 1] [productName {}] Creating job",
                    getProductName(mqiMessage));
            AppDataJobDto<LevelProductDto> appDataJob = buildJob(mqiMessage);

            // Dispatch job
            step++;
            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    getProductName(mqiMessage));
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
                    getProductName(mqiMessage), ace.getCode().getCode(),
                    ace.getLogMessage());
        }

        // Ack and check if application shall stopped
        ackProcessing(mqiMessage, ackOk, errorMessage);

        LOGGER.info("[MONITOR] [step 0] [productName {}] End",
                getProductName(mqiMessage));
    }

    protected AppDataJobDto<LevelProductDto> buildJob(
            GenericMessageDto<LevelProductDto> mqiMessage)
            throws AbstractCodedException {
        LevelProductDto leveldto = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        List<AppDataJobDto<LevelProductDto>> existingJobs = appDataService
                .findByMessagesIdentifier(mqiMessage.getIdentifier());

        if (CollectionUtils.isEmpty(existingJobs)) {
            // Job does not exists => create it
            Matcher m = l0SLicesPattern.matcher(leveldto.getProductName());
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
            AppDataJobDto<LevelProductDto> jobDto = new AppDataJobDto<>();
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
            productDto.setStartTime(DateUtils
                    .convertWithSimpleDateFormat(startTime, DATE_FORMAT));
            productDto.setStopTime(DateUtils
                    .convertWithSimpleDateFormat(stopTime, DATE_FORMAT));
            jobDto.setProduct(productDto);

            LOGGER.info(
                    "[REPORT] [MONITOR] [s1pdgsTask L1JobGeneration] [START] [productName {}] Starting job generation",
                    jobDto.getProduct().getProductName());

            return appDataService.newJob(jobDto);

        } else {
            // Update pod if needed
            AppDataJobDto<LevelProductDto> jobDto = existingJobs.get(0);
            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getIdentifier(), jobDto,
                        false, false, false);
            }
            // Job already exists
            return jobDto;
        }
    }

    protected String getProductName(
            final GenericMessageDto<LevelProductDto> dto) {
        return dto.getBody().getProductName();
    }

    @Override
    protected String getTaskForFunctionalLog() {
        return "L1JobGeneration";
    }
}
