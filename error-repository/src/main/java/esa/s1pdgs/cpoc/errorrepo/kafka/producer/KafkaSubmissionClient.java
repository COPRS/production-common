package esa.s1pdgs.cpoc.errorrepo.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, MqiGenericMessageDto<?>> client;
	
	public KafkaSubmissionClient(KafkaTemplate<String, MqiGenericMessageDto<?>> client) {
		this.client = client;
	}

	@Override
	public void resubmit(final FailedProcessingDto failedProcessing, final MqiGenericMessageDto<?> message) {        
        client.send(failedProcessing.getTopic(), message);
	}

}
