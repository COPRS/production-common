package fr.viveris.s1pdgs.archives.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
import fr.viveris.s1pdgs.archives.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.archives.services.L1SlicesS3Services;
import fr.viveris.s1pdgs.archives.services.L0SlicesS3Services;

@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "slice")
public class SlicesConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(SlicesConsumer.class);
	/**
	 * Amazon S3 service for configuration files
	 */
	private final L0SlicesS3Services l0SlicesS3Services;
	/**
	 * Amazon S3 service for configuration files
	 */
	private final L1SlicesS3Services l1SlicesS3Services;
	/**
	 * Path to the shared volume
	 */
	private final String sharedVolume;
	
	/**
	 * 
	 * @param l0SlicesS3Services
	 * @param l1SlicesS3Services
	 */
	public SlicesConsumer(final L0SlicesS3Services l0SlicesS3Services, final L1SlicesS3Services l1SlicesS3Services, 
			@Value("${kafka.topic.slices}") final String sharedVolume) {
		this.l0SlicesS3Services = l0SlicesS3Services;
		this.l1SlicesS3Services = l1SlicesS3Services;
		this.sharedVolume = sharedVolume;
	}

	@KafkaListener(topics = "${kafka.topic.slices}", groupId = "${kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
	public void receive(SliceDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [slice] [productName {}] Starting distribution of Slice", dto.getProductName());
		switch (dto.getProductName().charAt(12)) {
		case '0': // Slice L0
			try {
				if(this.l0SlicesS3Services.exist(dto.getKeyObjectStorage())) {
					this.l0SlicesS3Services.downloadFiles(dto.getKeyObjectStorage(), this.sharedVolume);
					LOGGER.info("[MONITOR] [Step 0] [slice] [productName {}] Slice distributed", dto.getProductName());
				}
				else {
					LOGGER.error("[MONITOR] [slice] [productName {}] Slice does not exists in Object Storage", dto.getProductName());
				}
			} catch (ObjectStorageException e) {
				LOGGER.error("[MONITOR] [slice] [productName {}] {}", dto.getProductName(), e.getMessage());
			}
			break;
		case '1': // Slice L1
			try {
				if(this.l1SlicesS3Services.exist(dto.getKeyObjectStorage())) {
					this.l1SlicesS3Services.downloadFiles(dto.getKeyObjectStorage(), this.sharedVolume);
					LOGGER.info("[MONITOR] [Step 0] [slice] [productName {}] Slice distributed", dto.getProductName());
				}
				else {
					LOGGER.error("[MONITOR] [slice] [productName {}] Slice does not exists in Object Storage", dto.getProductName());
				}
			} catch (ObjectStorageException e) {
				LOGGER.error("[MONITOR] [slice] [productName {}] {}", dto.getProductName(), e.getMessage());
			}
			break;
		default:
			LOGGER.error("[MONITOR] [slice] [productName {}] Slice level unknown", dto.getProductName());
			break;
		}
	}
}
