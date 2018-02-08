package fr.viveris.s1pdgs.mdcatalog.services.kafka;

import java.util.concurrent.CountDownLatch;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaMetadataDto;
import fr.viveris.s1pdgs.mdcatalog.services.es.EsServices;

/**
 * KAFKA consumer.
 * Consume on a topic defined in configuration file
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaConsumer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);

	@Autowired
	private EsServices esServices;

	/**
	 * Count down latch which allows the POJO to signal that a message is received
	 */
	private CountDownLatch latchMetadata = new CountDownLatch(1);

	/**
	 * Message listener container. Read a message
	 * 
	 * @param payload
	 */
	@KafkaListener(topics = "${kafka.topic.metadata}", groupId = "${kafka.group-id}")
	public void receive(KafkaMetadataDto metadata) {
		LOGGER.debug("[receive] Consume message {}", metadata);
		this.latchMetadata.countDown();
		// Create metadata
		if (metadata.getAction().equals("CREATE")) {
			try {
				JSONObject metadataToIndex = new JSONObject(metadata.getMetadata());
				if (esServices.isMetadataExist(metadataToIndex)) {
					esServices.createMetadata(metadataToIndex);
				}
				LOGGER.info("Metadata created for {}", metadataToIndex.getString("productName"));
			} catch (Exception e){
				LOGGER.error(e.getMessage());
			}
		} else {
			LOGGER.error("Invalid action {} for metadata {}", metadata.getAction(), metadata.getMetadata());
		}
	}
	
	public CountDownLatch getLatchMetadata() {
		return this.latchMetadata;
	}
}
