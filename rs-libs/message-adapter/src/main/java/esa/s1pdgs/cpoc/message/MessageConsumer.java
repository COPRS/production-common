package esa.s1pdgs.cpoc.message;

public interface MessageConsumer<M> {

    void onMessage(Message<M> message, Acknowledgement acknowledgement, Consumption consumption);

    Class<M> messageType();

    String topic();

}
