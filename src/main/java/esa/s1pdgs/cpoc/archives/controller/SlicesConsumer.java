package esa.s1pdgs.cpoc.archives.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.archives.DevProperties;
import esa.s1pdgs.cpoc.archives.services.ObsService;
import esa.s1pdgs.cpoc.archives.status.AppStatus;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;

/**
 * @author Viveris Technologies
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "slice")
public class SlicesConsumer {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(SlicesConsumer.class);
    /**
     * Service for Object Storage
     */
    private final ObsService obsService;

    /**
     * Path to the shared volume
     */
    private final String sharedVolume;

    /**
     * Object containing the dev properties
     */
    private final DevProperties devProperties;
    
    /**
     * Application status for archives
     */
    private final AppStatus appStatus;

    /**
     * @param l0SlicesS3Services
     * @param l1SlicesS3Services
     */
    public SlicesConsumer(final ObsService obsService,
            @Value("${file.slices.local-directory}") final String sharedVolume,
            final DevProperties devProperties, final AppStatus appStatus) {
        this.obsService = obsService;
        this.sharedVolume = sharedVolume;
        this.devProperties = devProperties;
        this.appStatus = appStatus;
    }

    @KafkaListener(topics = "#{'${kafka.topics.slices}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(final LevelProductDto dto,
            final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
        LOGGER.info(
                "[REPORT] [MONITOR] [step 0] [family {}] [productName {}] [s1pdgsTask Archiver] [START] Start distribution",
                dto.getFamily(), dto.getProductName());
        this.appStatus.setProcessing("SLICES");
        try {
            if (!devProperties.getActivations().get("download-all")) {
                this.obsService.downloadFile(dto.getFamily(),
                        dto.getKeyObjectStorage() + "/manifest.safe",
                        this.sharedVolume + "/"
                                + dto.getFamily().name().toLowerCase());
            } else {
                this.obsService.downloadFile(dto.getFamily(),
                        dto.getKeyObjectStorage(), this.sharedVolume + "/"
                                + dto.getFamily().name().toLowerCase());
            }
            acknowledgment.acknowledge();
        } catch (ObsException e) {
            LOGGER.error(
                    "[REPORT] [MONITOR] [step 0] [s1pdgsTask Archiver] [STOP KO] [family {}] [productName {}] [resuming {}] {}",
                    dto.getFamily(), dto.getProductName(),
                    new ResumeDetails(topic, dto), e.getMessage());
            this.appStatus.setError("SLICES");
        } catch (Exception exc) {
            LOGGER.error(
                    "[REPORT] [MONITOR] [step 0] [s1pdgsTask Archiver] [STOP KO] [family {}] [productName {}] [code {}] Exception occurred during acknowledgment {}",
                    dto.getFamily(), dto.getProductName(),
                    ErrorCode.INTERNAL_ERROR.getCode(), exc.getMessage());
            this.appStatus.setError("SLICES");
        }
        LOGGER.info(
                "[REPORT] [MONITOR] [step 0] [family {}] [productName {}] [s1pdgsTask Archiver] [STOP OK] End Distribution",
                dto.getFamily(), dto.getProductName());
        this.appStatus.setWaiting();
    }
}
