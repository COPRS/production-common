package esa.s1pdgs.cpoc.message.kafka.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.message.kafka.KafkaMessageProducer;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;

@Configuration
public class KafkaProducerConfiguration {

    private final KafkaProperties properties;

    @Autowired
    public KafkaProducerConfiguration(KafkaProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MessageProducer<AbstractMessage> messageProducer() {
        return new KafkaMessageProducer<>(new KafkaTemplate<>(producerFactory()));
    }

    /**
     * Producer configuration
     *
     */
    @Autowired
    private Map<String, Object> producerConfigs() {
        final Map<String, Object> props = new ConcurrentHashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG,
                properties.getProducer().getMaxRetries());
        return props;
    }

    private ProducerFactory<String, AbstractMessage> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
