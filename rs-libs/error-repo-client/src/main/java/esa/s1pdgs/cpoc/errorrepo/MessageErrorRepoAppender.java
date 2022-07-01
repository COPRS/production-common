package esa.s1pdgs.cpoc.errorrepo;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessing;
import esa.s1pdgs.cpoc.message.MessageProducer;

public class MessageErrorRepoAppender implements ErrorRepoAppender {

    private final String topic;
    private final MessageProducer<FailedProcessing> messageProducer;

    public MessageErrorRepoAppender(final String topic, final MessageProducer<FailedProcessing> messageProducer) {
        this.topic = topic;
        this.messageProducer = messageProducer;
    }

    @Override
    public void send(final FailedProcessing errorRequest) {
        try {
            messageProducer.send(topic, errorRequest);
        } catch (final Exception e) {
            throw new RuntimeException(
                    String.format("Error appending message to error queue '%s': %s", topic, Exceptions.messageOf(e)),
                    e
            );
        }
    }
}
