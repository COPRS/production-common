package fr.viveris.s1pdgs.archives.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
import fr.viveris.s1pdgs.archives.model.ResumeDetails;
import fr.viveris.s1pdgs.archives.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.archives.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.archives.services.ObsService;

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
     * @param l0SlicesS3Services
     * @param l1SlicesS3Services
     */
    public SlicesConsumer(final ObsService obsService,
            @Value("${file.slices.local-directory}") final String sharedVolume) {
        this.obsService = obsService;
        this.sharedVolume = sharedVolume;
    }

    @KafkaListener(topics = "#{'${kafka.topics.slices}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(final SliceDto dto,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
        LOGGER.info(
                "[MONITOR] [step 0] [family {}] [productName {}] Starting distribution",
                dto.getFamily(), dto.getProductName());
        try {
            this.obsService.downloadFile(dto.getFamily(),
                    dto.getKeyObjectStorage(), this.sharedVolume + "/"
                            + dto.getFamily().name().toLowerCase());
        } catch (ObjectStorageException | ObsUnknownObjectException e) {
            LOGGER.error(
                    "[MONITOR] [step 0] [family {}] [productName {}] [resuming {}] {}",
                    dto.getFamily(), dto.getProductName(),
                    new ResumeDetails(topic, dto), e.getMessage());
        }
        LOGGER.info("[MONITOR] [step 0] [family {}] [productName {}] End",
                dto.getFamily(), dto.getProductName());
    }
}
