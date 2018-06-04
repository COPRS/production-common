package fr.viveris.s1pdgs.ingestor.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.ingestor.exceptions.KafkaSendException;

/**
 * Implementation of the publication service using Kafka
 * @author Cyrielle Gailliard
 *
 * @param <T>
 */
public abstract class AbstractKafkaService<T> implements PublicationServices<T> {

	/**
	 * Logger
	 */
	private static final Logger LOGGER = LogManager.getLogger(KafkaConfigFileProducer.class);

	/**
	 * KAFKA template
	 */
	private final KafkaTemplate<String, T> kafkaTemplate;
	/**
	 * Name of the topic
	 */
	private final String kafkaTopic;

	/**
	 * Constructor
	 * 
	 * @param kafkaTemplate
	 * @param kafkaTopic
	 */
	public AbstractKafkaService(final KafkaTemplate<String, T> kafkaTemplate, final String kafkaTopic) {
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaTopic = kafkaTopic;
	}

	/**
	 * 
	 */
	@Override
	public void send(T obj) throws KafkaSendException {
		try {
			LOGGER.debug("[send] Send metadata = {}", obj);
			kafkaTemplate.send(kafkaTopic, obj).get();
		} catch (CancellationException | InterruptedException | ExecutionException e) {
			throw new KafkaSendException(kafkaTopic, extractProductName(obj), e.getMessage(), e);
		}
	}

	/**
	 * Extract the product name of the DTO
	 * 
	 * @param obj
	 * @return
	 */
	protected abstract String extractProductName(T obj);

	/**
	 * @return the kafkaTopic
	 */
	public String getKafkaTopic() {
		return kafkaTopic;
	}
}
