package esa.s1pdgs.cpoc.errorrepo.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, Object> client;
	
	public KafkaSubmissionClient(KafkaTemplate<String, Object> client) {
		this.client = client;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void resubmit(final FailedProcessingDto failedProcessing, final Object message) {    
        client.send(failedProcessing.getTopic(), message);
	}

}
