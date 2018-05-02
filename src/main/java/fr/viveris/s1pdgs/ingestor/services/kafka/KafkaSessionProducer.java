package fr.viveris.s1pdgs.ingestor.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

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

	/**
	 * Send a message asynchronously to a topic
	 * 
	 * @param descriptor
	 */
	public void sendAsynchrone(KafkaEdrsSessionDto descriptor) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[sendAsynchrone] Send ERDS session = {}", descriptor);
		}

		ListenableFuture<SendResult<String, KafkaEdrsSessionDto>> future = kafkaSessionTemplate.send(kafkaTopic,
				descriptor.getObjectStorageKey(), descriptor);

		// We register a callback to verify whether the messages are sent to the topic
		// successfully or not
		future.addCallback(new ListenableFutureCallback<SendResult<String, KafkaEdrsSessionDto>>() {
			@Override
			public void onSuccess(SendResult<String, KafkaEdrsSessionDto> result) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[sendAsynchrone] Success ERDS session = {}", descriptor);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				LOGGER.error("[sendAsynchrone] Failed: {}", e.getMessage());
			}
		});
	}

}
