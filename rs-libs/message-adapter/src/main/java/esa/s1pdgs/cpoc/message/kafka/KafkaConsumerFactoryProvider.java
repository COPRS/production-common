package esa.s1pdgs.cpoc.message.kafka;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.core.ConsumerFactory;

public interface KafkaConsumerFactoryProvider<M> {

    ConsumerFactory<String, M> consumerFactoryFor(Map<String, Object> configs, Deserializer<String> keyDeserializer, Deserializer<M> valueDeserializer);

}
