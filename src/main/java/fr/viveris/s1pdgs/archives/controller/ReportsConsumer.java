package fr.viveris.s1pdgs.archives.controller;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.archives.controller.dto.ReportDto;
import fr.viveris.s1pdgs.archives.utils.FileUtils;

@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "report")
public class ReportsConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(SlicesConsumer.class);
	/**
	 * Path to the shared volume
	 */
	private final String sharedVolume;
	
	public ReportsConsumer(@Value("${file.reports.local-directory}") final String sharedVolume) {
		this.sharedVolume = sharedVolume;
	}

	@KafkaListener(topics = "#{'${kafka.topics.reports}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "reportKafkaListenerContainerFactory")
	public void receive(ReportDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [reports] [productName {}] Starting distribution of Report",
				dto.getProductName());
		try {
			File report = new File(this.sharedVolume  + dto.getFamilyName().toLowerCase() + dto.getProductName());
			FileUtils.writeFile(report, dto.getContent());
			LOGGER.info("[MONITOR] [Step 0] [reports] [productName {}] Report distributed",	dto.getProductName());
		} catch (IOException e) {
			LOGGER.error("[MONITOR] [reports] [productName {}] {}", dto.getProductName(), e.getMessage());
		}
	}
}
