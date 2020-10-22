package esa.s1pdgs.cpoc.message;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public interface MessageConsumer<T extends AbstractMessage> {

    void onMessage(Message<T> message, Acknowledgement acknowledgement, Consumption consumption);

    Class<T> messageType();

    String topic();

}
