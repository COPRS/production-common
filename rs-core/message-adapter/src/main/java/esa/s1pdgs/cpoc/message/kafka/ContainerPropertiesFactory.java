package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;

public interface ContainerPropertiesFactory<M> {

    ContainerProperties containerPropertiesFor(final String topic, final MessageListener<String, M> messageListener);

}
