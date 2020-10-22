package esa.s1pdgs.cpoc.reqrepo.kafka.producer;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.message.MessageProducer;

public class MessageSubmissionClient implements SubmissionClient {
	
	private final MessageProducer<Object> messageProducer;

	public MessageSubmissionClient(MessageProducer<Object> messageProducer) {
		this.messageProducer = messageProducer;
	}

	@Override
	public void resubmit(final long failedProcessingId, final String topic, final Object message, final AppStatus appStatus) {    		
		try {
			messageProducer.send(topic, message);
		} catch (final Exception e) {
			final Throwable cause = Exceptions.unwrap(e);
			appStatus.getStatus().setFatalError();
			throw new RuntimeException(
					String.format(
							"Error restarting failedRequest '%s' on topic '%s': %s",
							failedProcessingId,
							topic, 
							Exceptions.messageOf(cause)
					),
					cause
			);
		}		
	}

}
