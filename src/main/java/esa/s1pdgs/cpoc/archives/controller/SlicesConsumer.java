package esa.s1pdgs.cpoc.archives.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.archives.DevProperties;
import esa.s1pdgs.cpoc.archives.controller.dto.SliceDto;
import esa.s1pdgs.cpoc.archives.model.ResumeDetails;
import esa.s1pdgs.cpoc.archives.model.exception.ObjectStorageException;
import esa.s1pdgs.cpoc.archives.model.exception.ObsUnknownObjectException;
import esa.s1pdgs.cpoc.archives.model.exception.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.archives.services.ObsService;

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
    
    private final DevProperties devProperties;

    /**
     * @param l0SlicesS3Services
     * @param l1SlicesS3Services
     */
    public SlicesConsumer(final ObsService obsService,
            @Value("${file.slices.local-directory}") final String sharedVolume,
            final DevProperties devProperties) {
        this.obsService = obsService;
        this.sharedVolume = sharedVolume;
        this.devProperties = devProperties;
    }

    @KafkaListener(topics = "#{'${kafka.topics.slices}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void receive(final SliceDto dto,
    		final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
        LOGGER.info(
                "[REPORT] [MONITOR] [step 0] [family {}] [productName {}] [s1pdgsTask Archiver] [START] Start distribution",
                dto.getFamily(), dto.getProductName());
        try {
            if(devProperties.getActivations().get("download-manifest")) {
                this.obsService.downloadFile(dto.getFamily(),
                        dto.getKeyObjectStorage()+ "/manifest.safe",
                        this.sharedVolume + "/" + 
                        dto.getFamily().name().toLowerCase());
            } else {
                this.obsService.downloadFile(dto.getFamily(),
                        dto.getKeyObjectStorage(), this.sharedVolume + "/"
                                + dto.getFamily().name().toLowerCase());
            }
            acknowledgment.acknowledge();
        } catch (ObjectStorageException | ObsUnknownObjectException e) {
            LOGGER.error(
                    "[REPORT] [MONITOR] [step 0] [s1pdgsTask Archiver] [STOP KO] [family {}] [productName {}] [resuming {}] {}",
                    dto.getFamily(), dto.getProductName(),
                    new ResumeDetails(topic, dto), e.getMessage());
        } catch (Exception exc) {
            LOGGER.error(
                    "[REPORT] [MONITOR] [step 0] [s1pdgsTask Archiver] [STOP KO] [family {}] [productName {}] [code {}] Exception occurred during acknowledgment {}",
                    dto.getFamily(), dto.getProductName(), ErrorCode.KAFKA_COMMIT_ERROR.getCode(),
                    exc.getMessage());
		}
        LOGGER.info("[REPORT] [MONITOR] [step 0] [family {}] [productName {}] [s1pdgsTask Archiver] [STOP OK] End Distribution",
                dto.getFamily(), dto.getProductName());
    }
}
