package fr.viveris.s1pdgs.ingestor.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.ingestor.model.exception.KafkaAuxFilesPublicationException;

/**
 * KAFKA producer for publishing metadata. </br>
 * Produce a message in topic of metadata.
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaConfigFileProducer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(KafkaConfigFileProducer.class);

	/**
	 * KAFKA template for topic "metadata"
	 */
	@Autowired
	private KafkaTemplate<String, KafkaConfigFileDto> kafkaMetadataTemplate;
	/**
	 * Name of the topic "metadata"
	 */
	@Value("${kafka.topic.auxiliary-files}")
	private String kafkaTopic;

	/**
	 * Send a message to a topic and wait until one is published
	 * 
	 * @param customer
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void send(KafkaConfigFileDto metadataCrud) throws KafkaAuxFilesPublicationException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send metadata = {}", metadataCrud);
			}
			kafkaMetadataTemplate.send(kafkaTopic, metadataCrud).get();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Success metadata = {}", metadataCrud);
			}
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaAuxFilesPublicationException(metadataCrud.getProductName(), e);
		}
	}

}
