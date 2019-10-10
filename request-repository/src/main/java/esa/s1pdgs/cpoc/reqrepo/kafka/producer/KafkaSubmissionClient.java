package esa.s1pdgs.cpoc.reqrepo.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, Object> client;
	
	public KafkaSubmissionClient(KafkaTemplate<String, Object> client) {
		this.client = client;
	}

	@Override
	public void resubmit(final FailedProcessing failedProcessing, final Object message) {    
        client.send(failedProcessing.getTopic(), message);
	}

}
