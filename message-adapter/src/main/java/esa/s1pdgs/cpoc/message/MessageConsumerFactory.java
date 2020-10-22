package esa.s1pdgs.cpoc.message;

import java.util.List;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public interface MessageConsumerFactory<T extends AbstractMessage> {

    List<MessageConsumer<T>> createConsumers();

}
