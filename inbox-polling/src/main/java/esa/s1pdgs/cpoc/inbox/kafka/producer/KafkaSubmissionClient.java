package esa.s1pdgs.cpoc.inbox.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, IngestionDto> client;
	private final String topic;
	
	public KafkaSubmissionClient(KafkaTemplate<String, IngestionDto> client, String topic) {
		this.client = client;
		this.topic = topic;
	}

	@Override
	public void publish(final IngestionDto dto) {    
        client.send(topic, dto);
	}

}
