package esa.s1pdgs.cpoc.archives.controller;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.archives.status.AppStatus;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException.ErrorCode;
import esa.s1pdgs.cpoc.common.errors.InternalErrorException;
import esa.s1pdgs.cpoc.common.utils.FileUtils;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;

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
     * Application status for archives
     */
    private final AppStatus appStatus;

    /**
     * Constructor
     * 
     * @param sharedVolume
     */
    public ReportsConsumer(
            @Value("${file.reports.local-directory}") final String sharedVolume,
            final AppStatus appStatus) {
        this.sharedVolume = sharedVolume;
        this.appStatus = appStatus;
    }

    /**
     * Consume message
     * 
     * @param dto
     * @param topic
     */
    @KafkaListener(topics = "#{'${kafka.topics.reports}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "reportKafkaListenerContainerFactory")
    public void receive(final LevelReportDto dto,
            final Acknowledgment acknowledgment,
            @Header(KafkaHeaders.RECEIVED_TOPIC) final String topic) {
        LOGGER.info(
                "[step 0] [family {}] [productName {}] Starting distribution",
                dto.getFamily(), dto.getProductName());
        this.appStatus.setProcessing("REPORTS");
        try {
            File report = new File(sharedVolume + File.separator
                    + dto.getFamily().name().toLowerCase() + File.separator
                    + dto.getProductName());
            FileUtils.writeFile(report, dto.getContent());
            acknowledgment.acknowledge();
        } catch (InternalErrorException iee) {
            LOGGER.error(
                    "[MONITOR] [step 0] [family {}] [productName {}] [code {}] [resuming {}] {}",
                    dto.getFamily(), dto.getProductName(),
                    iee.getCode().getCode(), new ResumeDetails(topic, dto),
                    iee.getLogMessage());
            this.appStatus.setError("REPORTS");
        } catch (Exception exc) {
            LOGGER.error(
                    "[MONITOR] [step 0] [family {}] [productName {}] [code {}] Exception occurred during acknowledgment {}",
                    dto.getFamily(), dto.getProductName(),
                    ErrorCode.INTERNAL_ERROR.getCode(), exc.getMessage());
            this.appStatus.setError("REPORTS");
        }

        LOGGER.info("[step 0] [family {}] End Distribution", dto.getFamily(),
                dto.getProductName());
        this.appStatus.setWaiting();
    }
}
