package esa.s1pdgs.cpoc.mdc.timer.publish;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

/**
 * Publisher to send CatalogEvents to a defined Kafka-Topic
 * @author Julian Kaping
 *
 */
public class KafkaPublisher implements Publisher {

	private KafkaTemplate<String, CatalogEvent> kafkaTemplate;
	private String topic;

	public KafkaPublisher(final KafkaTemplate<String, CatalogEvent> kafkaTemplate, String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Override
	public void publish(CatalogEvent event) throws Exception {
		try {
			kafkaTemplate.send(topic, event).get();
		} catch (final Exception e) {
			final Throwable cause = Exceptions.unwrap(e);
			throw new RuntimeException(String.format("Error on publishing CatalogEvent for %s to %s: %s",
					event.getProductName(), topic, Exceptions.messageOf(cause)), cause);
		}
	}

}
