package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import esa.s1pdgs.cpoc.message.Consumption;

public class KafkaConsumption<M> implements Consumption {

    private ConcurrentMessageListenerContainer<String, M> kafkaContainer;

    @Override
    public void pause() {
        kafkaContainer.pause();
    }

    @Override
    public void resume() {
        kafkaContainer.resume();
    }

    //need setter her because container is created after consumption
    public void setKafkaContainer(ConcurrentMessageListenerContainer<String, M> kafkaContainer) {
        this.kafkaContainer = kafkaContainer;
    }
}
