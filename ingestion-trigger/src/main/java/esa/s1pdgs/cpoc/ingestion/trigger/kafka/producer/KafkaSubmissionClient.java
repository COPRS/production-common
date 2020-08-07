package esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, IngestionJob> client;
	private final String topic;
	
	public KafkaSubmissionClient(final KafkaTemplate<String, IngestionJob> client, final String topic) {
		this.client = client;
		this.topic = topic;
	}

	@Override
	public void publish(final IngestionJob dto) {    
        try {
			client.send(topic, dto).get();
		} catch (final Exception e) {
			final Throwable cause = Exceptions.unwrap(e);
			throw new RuntimeException(
					String.format(
							"Error on publishing IngestionJob for %s to %s: %s", 
							dto.getProductName(),
							topic,
							Exceptions.messageOf(cause)
					),
					cause
			);
		}
	}

}
