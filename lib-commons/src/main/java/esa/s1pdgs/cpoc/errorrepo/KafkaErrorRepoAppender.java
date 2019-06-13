package esa.s1pdgs.cpoc.errorrepo;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class KafkaErrorRepoAppender implements ErrorRepoAppender {
	
	private final KafkaTemplate<String, FailedProcessingDto<?>> kafkaTemplate;
	private final String groupId;
	private final String topic;

	public KafkaErrorRepoAppender(KafkaTemplate<String, FailedProcessingDto<?>> kafkaTemplate, final String groupId, final String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.groupId = groupId;
		this.topic = topic;
	}

	@Override
	public void send(FailedProcessingDto<?> errorRequest) {		
		errorRequest.setGroup(groupId);
		kafkaTemplate.send(topic, errorRequest);
	}
}
