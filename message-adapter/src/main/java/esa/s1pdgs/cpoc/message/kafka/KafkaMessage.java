package esa.s1pdgs.cpoc.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import esa.s1pdgs.cpoc.message.Message;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class KafkaMessage<T extends AbstractMessage> implements Message<T> {

    private final T data;
    private final ConsumerRecord<String, T> kafkaRecord;

    public KafkaMessage(T data, ConsumerRecord<String, T> kafkaRecord) {
        this.data = data;
        this.kafkaRecord = kafkaRecord;
    }

    @Override
    public T data() {
        return data;
    }

    @Override
    public Object internalMessage() {
        return kafkaRecord;
    }
}
