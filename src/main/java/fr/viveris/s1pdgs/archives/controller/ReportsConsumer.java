package fr.viveris.s1pdgs.archives.controller;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.archives.controller.dto.ReportDto;
import fr.viveris.s1pdgs.archives.model.ResumeDetails;
import fr.viveris.s1pdgs.archives.model.exception.AbstractCodedException.ErrorCode;
import fr.viveris.s1pdgs.archives.utils.FileUtils;

/**
 * Consumer of reports
 * 
 * @author Viveris Technologies
 */
@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "report")
public class ReportsConsumer {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(ReportsConsumer.class);
    /**
     * Path to the shared volume
     */
    private final String sharedVolume;

    /**
     * Constructor
     * 
     * @param sharedVolume
     */
    public ReportsConsumer(
            @Value("${file.reports.local-directory}") final String sharedVolume) {
        this.sharedVolume = sharedVolume;
    }

    /**
     * Consume message
     * 
     * @param dto
     * @param topic
     */
    @KafkaListener(topics = "#{'${kafka.topics.reports}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "reportKafkaListenerContainerFactory")
    public void receive(final ReportDto dto,
    		final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
        LOGGER.info(
                "[MONITOR] [step 0] [family {}] [productName {}] Starting distribution",
                dto.getFamily(), dto.getProductName());
        try {
            File report = new File(sharedVolume + File.separator
                    + dto.getFamily().name().toLowerCase() + File.separator
                    + dto.getProductName());
            FileUtils.writeFile(report, dto.getContent());
            acknowledgment.acknowledge();
        } catch (IOException e) {
            LOGGER.error(
                    "[MONITOR] [step 0] [family {}] [productName {}] [resuming {}] {}",
                    dto.getFamily(), dto.getProductName(),
                    new ResumeDetails(topic, dto), e.getMessage());
        } catch (Exception exc) {
            LOGGER.error(
                    "[MONITOR] [step 0] [family {}] [productName {}] [code {}] Exception occurred during acknowledgment {}",
                    dto.getFamily(), dto.getProductName(), ErrorCode.KAFKA_COMMIT_ERROR.getCode(),
                    exc.getMessage());
        }
        
        LOGGER.info("[MONITOR] [step 0] [family {}] [productName {}] End",
                dto.getFamily(), dto.getProductName());
    }
}
