package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class KafkaMessageProducer<T extends AbstractMessage> implements MessageProducer<T> {

    private final KafkaTemplate<String, T> template;

    public KafkaMessageProducer(KafkaTemplate<String, T> template) {
        this.template = template;
    }

    @Override
    public void send(String topic, T message) {
        template.send(topic, message);
    }
}
