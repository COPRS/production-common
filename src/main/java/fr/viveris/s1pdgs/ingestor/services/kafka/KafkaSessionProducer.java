package fr.viveris.s1pdgs.ingestor.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaSessionDto;
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
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSessionProducer.class);

	/**
	 * KAFKA template for topic "session"
	 */
	@Autowired
	private KafkaTemplate<String, KafkaSessionDto> kafkaSessionTemplate;

	/**
	 * Name of the topic "session"
	 */
	@Value("${kafka.topic.sessions}")
	private String kafkaTopic;

	/**
	 * Send a message to a topic and wait until one is published
	 * 
	 * @param descriptor
	 */
	public void send(KafkaSessionDto descriptor) throws KafkaSessionPublicationException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send ERDS session = {}", descriptor);
			}
			kafkaSessionTemplate.send(kafkaTopic, descriptor).get();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Success RDS session = {}", descriptor);
			}
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaSessionPublicationException(descriptor.getProductName(), e);
		}
	}

	/**
	 * Send a message asynchronously to a topic
	 * 
	 * @param descriptor
	 */
	public void sendAsynchrone(KafkaSessionDto descriptor) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[sendAsynchrone] Send ERDS session = {}", descriptor);
		}

		ListenableFuture<SendResult<String, KafkaSessionDto>> future = kafkaSessionTemplate.send(kafkaTopic,
				descriptor.getSessionIdentifier(), descriptor);

		// We register a callback to verify whether the messages are sent to the topic
		// successfully or not
		future.addCallback(new ListenableFutureCallback<SendResult<String, KafkaSessionDto>>() {
			@Override
			public void onSuccess(SendResult<String, KafkaSessionDto> result) {
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
