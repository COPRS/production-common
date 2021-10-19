package esa.s1pdgs.cpoc.mdc.timer.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import esa.s1pdgs.cpoc.mqi.model.queue.CatalogEvent;

@EnableKafka
@Configuration
public class KafkaConfig {
    /**
     * URI of KAFKA cluster
     */
    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;
    
    @Value("${kafka.max-retries:10}")
    private int maxRetries;
    
    @Bean
    public KafkaTemplate<String, CatalogEvent> kafkaProducerClient()    {
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG, maxRetries);    
                 
        return new KafkaTemplate<String, CatalogEvent>(new DefaultKafkaProducerFactory<>(props));
    }
}
