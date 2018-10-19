package esa.s1pdgs.cpoc.jobgenerator.tasks.l0segmentapp;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Component
@ConditionalOnProperty(name = "process.level", havingValue = "L0_SEGMENT")

public class L0SegmentAppConsumer
        extends AbstractGenericConsumer<LevelSegmentDto> {

    /**
     * Pattern built from the regular expression given in configuration
     */
    private final Pattern pattern;
    private final Map<String, Integer> patternGroups;

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
    public L0SegmentAppConsumer(
            final AbstractJobsDispatcher<LevelSegmentDto> jobsDispatcher,
            @Value("${pattern.regexp}") final String patternRegexp,
            @Value("${pattern.groups}") final Map<String, Integer> patternGroups,
            final ProcessSettings processSettings,
            @Qualifier("mqiServiceForLevelSegments") final GenericMqiService<LevelSegmentDto> mqiService,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService,
            @Qualifier("appCatalogServiceForLevelSegments") final AbstractAppCatalogJobService<LevelSegmentDto> appDataService,
            final AppStatus appStatus) {
        super(jobsDispatcher, processSettings, mqiService, mqiStatusService,
                appDataService, appStatus);
        this.pattern = Pattern.compile(patternRegexp, Pattern.CASE_INSENSITIVE);
        this.patternGroups = patternGroups;
    }

    /**
     * Periodic function for processing messages
     */
    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}", initialDelayString = "${process.initial-delay-ms}")
    public void consumeMessages() {
        // First, consume message
        GenericMessageDto<LevelSegmentDto> mqiMessage = readMessage();
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }
        // process message
        appStatus.setProcessing(mqiMessage.getIdentifier());
        LOGGER.info(
                "[REPORT] [MONITOR] [step 0] [s1pdgsTask L0SegmentJobGeneration] [subTask Consume] [START] [productName {}] Starting job generation",
                getProductName(mqiMessage));
        int step = 1;
        boolean ackOk = false;
        String errorMessage = "";

        try {

            // Check if a job is already created for message identifier
            LOGGER.info("[MONITOR] [step 1] [productName {}] Creating/updating job",
                    getProductName(mqiMessage));
            AppDataJobDto<LevelSegmentDto> appDataJob = buildJob(mqiMessage);

            // Dispatch job
            step++;
            LOGGER.info(
                    "[MONITOR] [step 2] [productName {}] Dispatching product",
                    getProductName(mqiMessage));
            if (appDataJob.getState() == AppDataJobDtoState.WAITING || appDataJob.getState() == AppDataJobDtoState.DISPATCHING) {
                appDataJob.setState(AppDataJobDtoState.DISPATCHING);
                appDataJob = appDataService.patchJob(appDataJob.getIdentifier(),
                        appDataJob, false, false, false);
                jobsDispatcher.dispatch(appDataJob);
            } else {
                LOGGER.info(
                        "[MONITOR] [step 2] [productName {}] Job for datatake already dispatched",
                        getProductName(mqiMessage));
            }

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

    protected AppDataJobDto<LevelSegmentDto> buildJob(
            GenericMessageDto<LevelSegmentDto> mqiMessage)
            throws AbstractCodedException {
        LevelSegmentDto leveldto = mqiMessage.getBody();

        // Check if a job is already created for message identifier
        List<AppDataJobDto<LevelSegmentDto>> existingJobs = appDataService
                .findByMessagesIdentifier(mqiMessage.getIdentifier());

        if (CollectionUtils.isEmpty(existingJobs)) {

            // Extract information from name
            Matcher m = pattern.matcher(leveldto.getName());
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
            List<AppDataJobDto<LevelSegmentDto>> existingJobsForDatatake =
                    appDataService.findByProductDataTakeId(datatakeID);

            if (CollectionUtils.isEmpty(existingJobsForDatatake)) {

                // Create the JOB
                AppDataJobDto<LevelSegmentDto> jobDto = new AppDataJobDto<>();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                AppDataJobProductDto productDto = new AppDataJobProductDto();
                productDto.setAcquisition(acquisition);
                productDto.setMissionId(missionId);
                productDto.setDataTakeId(datatakeID);
                productDto.setProductName("l0_segments_for_" + datatakeID);
                productDto.setProcessMode(leveldto.getMode());
                productDto.setSatelliteId(satelliteId);
                jobDto.setProduct(productDto);

                LOGGER.info(
                        "[REPORT] [MONITOR] [s1pdgsTask L0SegmentJobGeneration] [START] [datatake {}] Starting job generation",
                        jobDto.getProduct().getDataTakeId());

                return appDataService.newJob(jobDto);
            } else {
                AppDataJobDto<LevelSegmentDto> jobDto = existingJobsForDatatake.get(0);
                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                }
                jobDto.getMessages().add(mqiMessage);
                return appDataService.patchJob(jobDto.getIdentifier(),
                        jobDto, true, false, false);

            }

        } else {
            // Update pod if needed
            AppDataJobDto<LevelSegmentDto> jobDto = existingJobs.get(0);
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
            final GenericMessageDto<LevelSegmentDto> dto) {
        return dto.getBody().getName();
    }

    @Override
    protected String getTaskForFunctionalLog() {
        return "L0SegmentJobGeneration";
    }
}
