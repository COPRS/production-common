package fr.viveris.s1pdgs.jobgenerator.tasks.consumer;

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
import esa.s1pdgs.cpoc.mqi.client.GenericMqiService;
import esa.s1pdgs.cpoc.mqi.client.StatusService;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.jobgenerator.config.ProcessSettings;
import fr.viveris.s1pdgs.jobgenerator.model.EdrsSessionFile;
import fr.viveris.s1pdgs.jobgenerator.service.EdrsSessionFileService;
import fr.viveris.s1pdgs.jobgenerator.status.AppStatus;
import fr.viveris.s1pdgs.jobgenerator.tasks.dispatcher.AbstractJobsDispatcher;

@Component
@ConditionalOnProperty(name = "process.level", havingValue = "L0")
public class EdrsSessionConsumer
        extends AbstractGenericConsumer<EdrsSessionDto> {
    /**
     * Service for EDRS session file
     */
    private final EdrsSessionFileService edrsService;

    @Autowired
    public EdrsSessionConsumer(final AbstractJobsDispatcher<EdrsSessionDto> jobDispatcher,
            final ProcessSettings processSettings,
            @Qualifier("mqiServiceForEdrsSessions") final GenericMqiService<EdrsSessionDto> mqiService,
            final EdrsSessionFileService edrsService,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService,
            @Qualifier("appCatalogServiceForEdrsSessions") final AbstractAppCatalogJobService<EdrsSessionDto> appDataService,
            final AppStatus appStatus) {
        super(jobDispatcher,
                processSettings, mqiService, mqiStatusService, appDataService,
                appStatus);
        this.edrsService = edrsService;
    }

    @Scheduled(fixedDelayString = "${process.fixed-delay-ms}")
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
                    "[MONITOR] [step {}] [productName {}] Starting job generation",
                    step, leveldto.getObjectStorageKey());
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
                    appDataService.patchJob(appDataJob.getIdentifier(),
                            AppDataJobDtoState.DISPATCHING,
                            appDataJob.getPod());
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
            productDto.setSatelliteId(mqiMessage.getBody().getSatelliteId());
            productDto.setStartTime(file.getStartTime());
            productDto.setStopTime(file.getStopTime());
            if (mqiMessage.getBody().getChannelId() == 1) {
                productDto.setRaws1(file.getRawNames().stream()
                        .map(rawI -> new AppDataJobFileDto(rawI.getFileName()))
                        .collect(Collectors.toList()));
            } else {
                productDto.setRaws2(file.getRawNames().stream()
                        .map(rawI -> new AppDataJobFileDto(rawI.getFileName()))
                        .collect(Collectors.toList()));
            }

            jobDto.setProduct(productDto);
            return appDataService.newJob(jobDto);

        } else {
            // Update pod if needed
            AppDataJobDto<EdrsSessionDto> jobDto = existingJobs.get(0);
            if (!jobDto.getPod().equals(processSettings.getHostname())) {
                jobDto.setPod(processSettings.getHostname());
                jobDto = appDataService.patchJob(jobDto.getIdentifier(),
                        jobDto);
            }
            // Updates messages if needed
            if (jobDto.getMessages().size() == 1 && jobDto.getMessages().get(0)
                    .getBody()
                    .getChannelId() != mqiMessage.getBody().getChannelId()) {
                EdrsSessionFile file = edrsService.createSessionFile(
                        mqiMessage.getBody().getObjectStorageKey());
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
                jobDto = appDataService.patchJob(jobDto.getIdentifier(),
                        jobDto);
            }
            // Retrun object
            return jobDto;
        }

    }

    @Override
    protected String getProductName(GenericMessageDto<EdrsSessionDto> dto) {
        return dto.getBody().getObjectStorageKey();
    }
}
