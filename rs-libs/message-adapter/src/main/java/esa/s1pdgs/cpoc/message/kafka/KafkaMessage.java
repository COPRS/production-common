package esa.s1pdgs.cpoc.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import esa.s1pdgs.cpoc.message.Message;

public class KafkaMessage<M> implements Message<M> {

    private final M data;
    private final ConsumerRecord<String, M> kafkaRecord;

    public KafkaMessage(M data, ConsumerRecord<String, M> kafkaRecord) {
        this.data = data;
        this.kafkaRecord = kafkaRecord;
    }

    @Override
    public M data() {
        return data;
    }

    @Override
    public Object internalMessage() {
        return kafkaRecord;
    }
}
