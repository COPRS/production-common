package fr.viveris.s1pdgs.archives.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
import fr.viveris.s1pdgs.archives.model.ProductFamily;
import fr.viveris.s1pdgs.archives.model.exception.ObjectStorageException;
import fr.viveris.s1pdgs.archives.model.exception.ObsUnknownObjectException;
import fr.viveris.s1pdgs.archives.services.ObsService;

@Component
@ConditionalOnProperty(prefix = "kafka.enable-consumer", name = "slice")
public class SlicesConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(SlicesConsumer.class);
	/**
	 * Service for Object Storage
	 */
	private final ObsService obsService;
	/**
	 * Path to the shared volume
	 */
	private final String sharedVolume;
	
	/**
	 * 
	 * @param l0SlicesS3Services
	 * @param l1SlicesS3Services
	 */
	public SlicesConsumer(final ObsService obsService, 
			@Value("${file.slices.local-directory}") final String sharedVolume) {
		this.obsService = obsService;
		this.sharedVolume = sharedVolume;
	}

	@KafkaListener(topics = "#{'${kafka.topics.slices}'.split(',')}", groupId = "${kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
	public void receive(SliceDto dto) {
		LOGGER.info("[MONITOR] [Step 0] [Slice] [productName {}] Starting distribution of Slice", dto.getProductName());
		switch (dto.getFamilyName()) {
		case "L0_PRODUCT": // Slice L0
			try {
				if(this.obsService.exist(ProductFamily.L0_PRODUCT, dto.getKeyObjectStorage())) {
					this.obsService.downloadFile(ProductFamily.L0_PRODUCT, dto.getKeyObjectStorage(), this.sharedVolume + "/" + dto.getFamilyName().toLowerCase());
					LOGGER.info("[MONITOR] [Step 0] [L0 Slice] [productName {}] Slice distributed", dto.getProductName());
				}
				else {
					LOGGER.error("[MONITOR] [L0 Slice] [productName {}] Slice does not exists in Object Storage", dto.getProductName());
				}
			} catch (ObjectStorageException | ObsUnknownObjectException e) {
				LOGGER.error("[MONITOR] [L0 Slice] [productName {}] {}", dto.getProductName(), e.getMessage());
			}
			break;
		case "L1_PRODUCT": // Slice L1
			try {
				if(this.obsService.exist(ProductFamily.L1_PRODUCT, dto.getKeyObjectStorage())) {
					this.obsService.downloadFile(ProductFamily.L1_PRODUCT, dto.getKeyObjectStorage(), this.sharedVolume + "/" + dto.getFamilyName().toLowerCase());
					LOGGER.info("[MONITOR] [Step 0] [L1 Slice] [productName {}] Slice distributed", dto.getProductName());
				}
				else {
					LOGGER.error("[MONITOR] [L1 Slice] [productName {}] Slice does not exists in Object Storage", dto.getProductName());
				}
			} catch (ObjectStorageException | ObsUnknownObjectException e) {
				LOGGER.error("[MONITOR] [L1 Slice] [productName {}] {}", dto.getProductName(), e.getMessage());
			}
			break;
		default:
			LOGGER.error("[MONITOR] [Slice] [productName {}] Slice level unknown", dto.getProductName());
			break;
		}
	}
}
