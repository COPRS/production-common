package esa.s1pdgs.cpoc.errorrepo;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class KafkaErrorRepoAppender implements ErrorRepoAppender {
	
	private final KafkaTemplate<String, FailedProcessingDto> kafkaTemplate;
	private final String topic;

	public KafkaErrorRepoAppender(KafkaTemplate<String, FailedProcessingDto> kafkaTemplate, final String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Override
	public void send(FailedProcessingDto errorRequest) {
		kafkaTemplate.send(topic, errorRequest);
	}
}
