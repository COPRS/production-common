package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.message.MessageProducer;

public class KafkaMessageProducer<M> implements MessageProducer<M> {

    private final KafkaTemplate<String, M> template;

    public KafkaMessageProducer(KafkaTemplate<String, M> template) {
        this.template = template;
    }

    @Override
    public void send(String topic, M message) {
        try {
            template.send(topic, message).get();
        } catch (final Exception e) {
            final Throwable cause = Exceptions.unwrap(e);
            throw new RuntimeException(
                    String.format(
                            "Error on publishing %s %s to %s: %s",
                            message.getClass().getSimpleName(),
                            message,
                            topic,
                            Exceptions.messageOf(cause)
                    ),
                    cause
            );
        }
    }

    @Override
    public String toString() {
        return "KafkaMessageProducer";
    }
}
