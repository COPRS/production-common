package esa.s1pdgs.cpoc.message;

import java.util.List;

public interface MessageConsumerFactory<M> {

    List<MessageConsumer<M>> createConsumers();

}
