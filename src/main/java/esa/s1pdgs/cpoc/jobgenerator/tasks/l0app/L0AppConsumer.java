package esa.s1pdgs.cpoc.jobgenerator.tasks.l0app;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.job.AbstractAppCatalogJobService;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobDtoState;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobFileDto;
import esa.s1pdgs.cpoc.appcatalog.common.rest.model.job.AppDataJobProductDto;
import esa.s1pdgs.cpoc.common.EdrsSessionFileType;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.InvalidFormatProduct;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.jobgenerator.config.ProcessSettings;
import esa.s1pdgs.cpoc.jobgenerator.model.EdrsSessionFile;
import esa.s1pdgs.cpoc.jobgenerator.service.EdrsSessionFileService;
import esa.s1pdgs.cpoc.jobgenerator.status.AppStatus;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractGenericConsumer;
import esa.s1pdgs.cpoc.jobgenerator.tasks.AbstractJobsDispatcher;
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;

@Component
@ConditionalOnProperty(name = "process.level", havingValue = "L0")
public class L0AppConsumer extends AbstractGenericConsumer<EdrsSessionDto> {
    /**
     * Service for EDRS session file
     */
    private final EdrsSessionFileService edrsService;

    @Autowired
    public L0AppConsumer(
            final AbstractJobsDispatcher<EdrsSessionDto> jobDispatcher,
            final ProcessSettings processSettings,
            @Qualifier("mqiServiceForEdrsSessions") final GenericMqiService<EdrsSessionDto> mqiService,
            final EdrsSessionFileService edrsService,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService,
            @Qualifier("appCatalogServiceForEdrsSessions") final AbstractAppCatalogJobService<EdrsSessionDto> appDataService,
            final AppStatus appStatus) {
        super(jobDispatcher, processSettings, mqiService, mqiStatusService,
                appDataService, appStatus);
        this.edrsService = edrsService;
    }

    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}", initialDelayString = "${process.initial-delay-ms}")
    public void consumeMessages() {
        // First, consume message
        GenericMessageDto<EdrsSessionDto> mqiMessage = readMessage();
        if (mqiMessage == null || mqiMessage.getBody() == null) {
            LOGGER.trace("[MONITOR] [step 0] No message received: continue");
            return;
        }

        // Second process message
        EdrsSessionDto leveldto = mqiMessage.getBody();

        if (leveldto.getProductType() == EdrsSessionFileType.SESSION) {

            int step = 0;
            boolean ackOk = false;
            String errorMessage = "";
            LOGGER.info(
                    "[REPORT] [MONITOR] [step {}] [s1pdgsTask L0JobGeneration] [subTask Consume] [START] [productName {}] Starting job generation",
                    step, getProductName(mqiMessage));
            appStatus.setProcessing(mqiMessage.getIdentifier());

            try {

                // Create the EdrsSessionFile object from the consumed message
                step = 1;
                LOGGER.info(
                        "[MONITOR] [step {}] [productName {}] Building product",
                        step, leveldto.getObjectStorageKey());
                if (leveldto.getChannelId() != 1
                        && leveldto.getChannelId() != 2) {
                    throw new InvalidFormatProduct("Invalid channel identifier "
                            + leveldto.getChannelId());
                }
                AppDataJobDto<EdrsSessionDto> appDataJob = buildJob(mqiMessage);

                // Dispatch
                step++;
                if (appDataJob.getMessages().size() == 2) {
                    LOGGER.info(
                            "[MONITOR] [step 2] [productName {}] Dispatching product",
                            getProductName(mqiMessage));
                    if (appDataJob.getState() == AppDataJobDtoState.WAITING) {
                        appDataJob.setState(AppDataJobDtoState.DISPATCHING);
                        appDataJob = appDataService.patchJob(
                                appDataJob.getIdentifier(), appDataJob, false,
                                false, false);
                    }
                    jobsDispatcher.dispatch(appDataJob);
                }

                // Ack
                step++;
                ackOk = true;

            } catch (AbstractCodedException ace) {
                ackOk = false;
                errorMessage = String.format(
                        "[MONITOR] [step %d] [productName %s] [code %d] %s",
                        step, getProductName(mqiMessage),
                        ace.getCode().getCode(), ace.getLogMessage());
            }

            // Ack and check if application shall stopped
            ackProcessing(mqiMessage, ackOk, errorMessage);

            step = 0;
            LOGGER.info("[MONITOR] [step 0] [productName {}] End", step,
                    leveldto.getObjectStorageKey());

        }

    }

    protected AppDataJobDto<EdrsSessionDto> buildJob(
            GenericMessageDto<EdrsSessionDto> mqiMessage)
            throws AbstractCodedException {
        // Check if a job is already created for message identifier
        List<AppDataJobDto<EdrsSessionDto>> existingJobs = appDataService
                .findByMessagesIdentifier(mqiMessage.getIdentifier());

        if (CollectionUtils.isEmpty(existingJobs)) {
            EdrsSessionFile file = edrsService.createSessionFile(
                    mqiMessage.getBody().getObjectStorageKey());

            // Search if session is already in progress
            List<AppDataJobDto<EdrsSessionDto>> existingJobsForSession =
                    appDataService.findByProductSessionId(file.getSessionId());

            if (CollectionUtils.isEmpty(existingJobsForSession)) {

                // Create the JOB
                AppDataJobDto<EdrsSessionDto> jobDto = new AppDataJobDto<>();
                // General details
                jobDto.setLevel(processSettings.getLevel());
                jobDto.setPod(processSettings.getHostname());
                // Messages
                jobDto.getMessages().add(mqiMessage);
                // Product
                AppDataJobProductDto productDto = new AppDataJobProductDto();
                productDto.setSessionId(file.getSessionId());
                productDto.setMissionId(mqiMessage.getBody().getMissionId());
                productDto.setProductName(file.getSessionId());
                productDto
                        .setSatelliteId(mqiMessage.getBody().getSatelliteId());
                productDto.setStartTime(DateUtils.convertToAnotherFormat(
                        file.getStartTime(), EdrsSessionFile.TIME_FORMATTER,
                        AppDataJobProductDto.TIME_FORMATTER));
                productDto.setStopTime(DateUtils.convertToAnotherFormat(
                        file.getStopTime(), EdrsSessionFile.TIME_FORMATTER,
                        AppDataJobProductDto.TIME_FORMATTER));
                if (mqiMessage.getBody().getChannelId() == 1) {
                    productDto.setRaws1(file.getRawNames().stream().map(
                            rawI -> new AppDataJobFileDto(rawI.getFileName()))
                            .collect(Collectors.toList()));
                } else {
                    productDto.setRaws2(file.getRawNames().stream().map(
                            rawI -> new AppDataJobFileDto(rawI.getFileName()))
                            .collect(Collectors.toList()));
                }

                jobDto.setProduct(productDto);
                LOGGER.info(
                        "[REPORT] [MONITOR] [s1pdgsTask L0JobGeneration] [START] [productName {}] Starting job generation",
                        jobDto.getProduct().getProductName());
                return appDataService.newJob(jobDto);

            } else {

                // Update pod if needed
                boolean update = false;
                boolean updateMessage = false;
                boolean updateProduct = false;
                AppDataJobDto<EdrsSessionDto> jobDto =
                        existingJobsForSession.get(0);
                if (!jobDto.getPod().equals(processSettings.getHostname())) {
                    jobDto.setPod(processSettings.getHostname());
                    update = true;
                }
                // Updates messages if needed
                if (jobDto.getMessages().size() == 1 && jobDto.getMessages()
                        .get(0).getBody().getChannelId() != mqiMessage.getBody()
                                .getChannelId()) {
                    jobDto.getMessages().add(mqiMessage);
                    if (mqiMessage.getBody().getChannelId() == 1) {
                        jobDto.getProduct()
                                .setRaws1(file.getRawNames().stream()
                                        .map(rawI -> new AppDataJobFileDto(
                                                rawI.getFileName()))
                                        .collect(Collectors.toList()));
                    } else {
                        jobDto.getProduct()
                                .setRaws2(file.getRawNames().stream()
                                        .map(rawI -> new AppDataJobFileDto(
                                                rawI.getFileName()))
                                        .collect(Collectors.toList()));
                    }
                    update = true;
                    updateMessage = true;
                    updateProduct = true;
                }
                // Update
                if (update) {
                    jobDto = appDataService.patchJob(jobDto.getIdentifier(),
                            jobDto, updateMessage, updateProduct, false);
                }
                // Return object
                return jobDto;

            }

        } else {
            // Update pod if needed
            boolean update = false;
            boolean updateMessage = false;
            boolean updateProduct = false;
            AppDataJobDto<EdrsSessionDto> jobDto = existingJobs.get(0);
            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                update = true;
            }
            // Update
            if (update) {
                jobDto = appDataService.patchJob(jobDto.getIdentifier(), jobDto,
                        updateMessage, updateProduct, false);
            }
            // Retrun object
            return jobDto;
        }

    }

    @Override
    protected String getProductName(GenericMessageDto<EdrsSessionDto> dto) {
        return dto.getBody().getObjectStorageKey();
    }

    @Override
    protected String getTaskForFunctionalLog() {
        return "L0JobGeneration";
    }
}
