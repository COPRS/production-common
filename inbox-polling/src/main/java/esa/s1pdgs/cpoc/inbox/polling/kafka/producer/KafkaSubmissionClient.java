package esa.s1pdgs.cpoc.inbox.polling.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, IngestionDto> client;
	
	public KafkaSubmissionClient(KafkaTemplate<String, IngestionDto> client) {
		this.client = client;
	}

	@Override
	public void resubmit(final String topic, final IngestionDto dto) {    
        client.send(topic, dto);
	}

}
