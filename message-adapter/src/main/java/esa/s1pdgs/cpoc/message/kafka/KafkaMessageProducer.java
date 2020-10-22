package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.message.MessageProducer;

public class KafkaMessageProducer<M> implements MessageProducer<M> {

    private final KafkaTemplate<String, M> template;

    public KafkaMessageProducer(KafkaTemplate<String, M> template) {
        this.template = template;
    }

    @Override
    public void send(String topic, M message) {
        template.send(topic, message);
    }
}
