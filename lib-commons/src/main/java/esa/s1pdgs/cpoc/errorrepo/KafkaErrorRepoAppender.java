package esa.s1pdgs.cpoc.errorrepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class KafkaErrorRepoAppender implements ErrorRepoAppender {
	
	private final KafkaTemplate<String, FailedProcessingDto<?>> kafkaTemplate;
	
	@Autowired	
	public KafkaErrorRepoAppender(KafkaTemplate<String, FailedProcessingDto<?>> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}

	@Override
	public void send(FailedProcessingDto<?> errorRequest) {		
		kafkaTemplate.send(FailedProcessingDto.TOPIC, errorRequest);
	}
}
