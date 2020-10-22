package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import esa.s1pdgs.cpoc.message.Consumption;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

public class KafkaConsumption implements Consumption {

    private ConcurrentMessageListenerContainer<String, ? extends AbstractMessage> kafkaContainer;

    @Override
    public void pause() {
        kafkaContainer.pause();
    }

    @Override
    public void resume() {
        kafkaContainer.resume();
    }

    //need setter her because container is created after consumption
    public void setKafkaContainer(ConcurrentMessageListenerContainer<String, ? extends AbstractMessage> kafkaContainer) {
        this.kafkaContainer = kafkaContainer;
    }
}
