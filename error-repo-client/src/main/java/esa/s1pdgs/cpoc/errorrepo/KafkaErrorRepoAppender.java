package esa.s1pdgs.cpoc.errorrepo;

import java.util.concurrent.ExecutionException;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;

public class KafkaErrorRepoAppender implements ErrorRepoAppender {
	
	private final KafkaTemplate<String, FailedProcessingDto> kafkaTemplate;
	private final String topic;

	public KafkaErrorRepoAppender(final KafkaTemplate<String, FailedProcessingDto> kafkaTemplate, final String topic) {
		this.kafkaTemplate = kafkaTemplate;
		this.topic = topic;
	}

	@Override
	public void send(final FailedProcessingDto errorRequest) {	
		try {
			sendAndWait(errorRequest);
		} catch (final Exception e) {
			final Throwable cause = Exceptions.unwrap(e);
			throw new RuntimeException(
					String.format("Error appending message to error queue '%s': %s", topic, Exceptions.messageOf(cause)),
					cause
			);
		}
	}
	
	private final void sendAndWait(final FailedProcessingDto errorRequest) throws InterruptedException, ExecutionException {
		final ListenableFuture<SendResult<String, FailedProcessingDto>> result = kafkaTemplate.send(
				topic, 
				errorRequest
		);
		result.get();
	}
}
