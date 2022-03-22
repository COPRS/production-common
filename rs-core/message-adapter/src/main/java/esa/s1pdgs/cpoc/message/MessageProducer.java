package esa.s1pdgs.cpoc.message;

public interface MessageProducer<M> {

    void send(String topic, M message);
}
