package esa.s1pdgs.cpoc.errorrepo.kafka.consumption;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import esa.s1pdgs.cpoc.errorrepo.model.rest.FailedProcessingDto;


@EnableKafka
@Configuration
public class ErrorQueueConsumerConfig {

    /**
     * URI of KAFKA cluster
     */
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    /**
     * Group identifier for KAFKA
     */
    @Value("${kafka.group-id}")
    private String kafkaGroupId;
    
    /**
     * Pool timeout for consumption
     */
    @Value("${kafka.poll-timeout}")
    private long kafkaPooltimeout;

    /**
     * Consumer configuration
     * 
     * @return
     */
    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        return props;
    }

    /**
     * Consumer factory
     * 
     * @return
     */
    @Bean
    public ConsumerFactory<String, FailedProcessingDto> consumerFactory() {
    	return new DefaultKafkaConsumerFactory<String,FailedProcessingDto>(consumerConfigs(), 
    			new StringDeserializer(),
                new JsonDeserializer<>(FailedProcessingDto.class));
    }

    /**
     * Listener containers factory
     * 
     * @return
     */
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, FailedProcessingDto>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, FailedProcessingDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
        factory.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
        return factory;
    }

}
