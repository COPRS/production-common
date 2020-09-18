package esa.s1pdgs.cpoc.odip.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.utils.Exceptions;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, Object> client;
	
	private final String topic;
	
	public KafkaSubmissionClient(final KafkaTemplate<String, Object> client, final String topic) {
		this.client = client;
		this.topic = topic;
	}

	@Override
	public void resubmit(final Object message, final AppStatus appStatus) {    		
		try {
			client.send(topic, message).get();
		} catch (final Exception e) {
			final Throwable cause = Exceptions.unwrap(e);
			appStatus.getStatus().setFatalError();
			throw new RuntimeException(
					String.format(
							"Error (re)starting request on topic '%s': %s",
							topic, 
							Exceptions.messageOf(cause)
					),
					cause
			);
		}		
	}

}
