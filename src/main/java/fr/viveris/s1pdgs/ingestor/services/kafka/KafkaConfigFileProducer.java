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

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.ingestor.model.exception.KafkaMetadataPublicationException;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConfigFileProducer.class);

	/**
	 * KAFKA template for topic "metadata"
	 */
	@Autowired
	private KafkaTemplate<String, KafkaConfigFileDto> kafkaMetadataTemplate;
	/**
	 * Name of the topic "metadata"
	 */
	@Value("${kafka.topic.config-files}")
	private String kafkaTopic;

	/**
	 * Send a message to a topic and wait until one is published
	 * 
	 * @param customer
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void send(KafkaConfigFileDto metadataCrud) throws KafkaMetadataPublicationException {
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Send metadata = {}", metadataCrud);
			}
			kafkaMetadataTemplate.send(kafkaTopic, metadataCrud).get();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("[send] Success metadata = {}", metadataCrud);
			}
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaMetadataPublicationException(metadataCrud.getProductName(), e);
		}
	}

	/**
	 * Send a message asynchronously to a topic
	 * 
	 * @param metadataCrud
	 */
	public void sendAsynchrone(KafkaConfigFileDto metadataCrud) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("[sendAsynchrone] Send metadata = {}", metadataCrud);
		}

		ListenableFuture<SendResult<String, KafkaConfigFileDto>> future = kafkaMetadataTemplate.send(kafkaTopic,
				metadataCrud);

		// We register a callback to verify whether the messages are sent to the topic
		// successfully or not
		future.addCallback(new ListenableFutureCallback<SendResult<String, KafkaConfigFileDto>>() {
			@Override
			public void onSuccess(SendResult<String, KafkaConfigFileDto> result) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("[sendAsynchrone] Success metadata = {}", metadataCrud);
				}
			}

			@Override
			public void onFailure(Throwable e) {
				LOGGER.error("[sendAsynchrone] Failed: {}", e.getMessage());
			}
		});
	}

}
