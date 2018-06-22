package fr.viveris.s1pdgs.archives.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
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
		try {
			if(this.obsService.exist(dto.getFamily(), dto.getKeyObjectStorage())) {
				this.obsService.downloadFile(dto.getFamily(), dto.getKeyObjectStorage(), this.sharedVolume + "/" + dto.getFamily().toString().toLowerCase());
				LOGGER.info("[MONITOR] [Step 0] [{}] [productName {}] Slice distributed",dto.getFamily(),  dto.getProductName());
			}
			else {
				LOGGER.error("[MONITOR] [{}] [productName {}] Slice does not exists in Object Storage",dto.getFamily(),  dto.getProductName());
			}
		} catch (ObjectStorageException | ObsUnknownObjectException e) {
			LOGGER.error("[MONITOR] [{}] [productName {}] {}", dto.getFamily(),  dto.getProductName(), e.getMessage());
		}
	}
}
