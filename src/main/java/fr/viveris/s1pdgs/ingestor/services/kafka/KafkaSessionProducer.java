package fr.viveris.s1pdgs.ingestor.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.ingestor.model.exception.KafkaSessionPublicationException;

/**
 * KAFKA producer for publishing session information. </br>
 * Produce a message in topic of session.
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaSessionProducer {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(KafkaSessionProducer.class);

	/**
	 * KAFKA template for topic "session"
	 */
	@Autowired
	private KafkaTemplate<String, KafkaEdrsSessionDto> kafkaSessionTemplate;

	/**
	 * Name of the topic "session"
	 */
	@Value("${kafka.topic.edrs-sessions}")
	private String kafkaTopic;

	/**
	 * Send a message to a topic and wait until one is published
	 * 
	 * @param descriptor
	 */
	public void send(KafkaEdrsSessionDto descriptor) throws KafkaSessionPublicationException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send ERDS session = {}", descriptor);
			}
			kafkaSessionTemplate.send(kafkaTopic, descriptor).get();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Success RDS session = {}", descriptor);
			}
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaSessionPublicationException(descriptor.getObjectStorageKey(), e);
		}
	}
}
