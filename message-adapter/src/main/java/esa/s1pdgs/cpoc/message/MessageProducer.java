package esa.s1pdgs.cpoc.message;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public interface MessageProducer<T extends AbstractMessage> {

    void send(String topic, T message);
}
