package esa.s1pdgs.cpoc.reqrepo.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.appcatalog.common.FailedProcessing;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.utils.Exceptions;

public class KafkaSubmissionClient implements SubmissionClient {
	
	private final KafkaTemplate<String, Object> client;
	
	public KafkaSubmissionClient(final KafkaTemplate<String, Object> client) {
		this.client = client;
	}

	@Override
	public void resubmit(final FailedProcessing failedProcessing, final Object message, final AppStatus appStatus) {    		
		try {
			client.send(failedProcessing.getTopic(), message).get();
		} catch (final Exception e) {
			final Throwable cause = Exceptions.unwrap(e);
			appStatus.getStatus().setFatalError();
			throw new RuntimeException(
					String.format(
							"Error restarting failedRequest '%s' on topic '%s': %s",
							failedProcessing.getId(),
							failedProcessing.getTopic(), 
							Exceptions.messageOf(cause)
					),
					cause
			);
		}		
	}

}
