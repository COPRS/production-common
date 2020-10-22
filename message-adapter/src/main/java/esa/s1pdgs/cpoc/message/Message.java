package esa.s1pdgs.cpoc.message;

import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public interface Message<T extends AbstractMessage> {

    T data();

    Object internalMessage();

}
