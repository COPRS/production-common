package esa.s1pdgs.cpoc.message.kafka.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.lang.Nullable;

import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.message.kafka.KafkaMessageProducer;
import esa.s1pdgs.cpoc.message.kafka.ProducerConfigurationFactory;

@Configuration
public class KafkaProducerConfiguration<M> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerConfiguration.class);

    private final KafkaProperties properties;
    private final ProducerConfigurationFactory producerConfigurationFactory;

    @Autowired
    public KafkaProducerConfiguration(
            final KafkaProperties properties,
            @Nullable final ProducerConfigurationFactory producerConfigurationFactory) {

        this.properties = properties;
        this.producerConfigurationFactory = producerConfigurationFactory;
    }

    @Bean
    public MessageProducer<M> messageProducer() {
        return new KafkaMessageProducer<>(new KafkaTemplate<>(producerFactory()));
    }

    /**
     * Producer configuration
     */
    private Map<String, Object> producerConfigs() {
    	LOG.info("producerConfigs: {}", properties);
    	
        final Map<String, Object> props = new ConcurrentHashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG,
                properties.getMaxRetries());

        if (producerConfigurationFactory != null) {
            props.putAll(producerConfigurationFactory.producerConfiguration());
        }

        LOG.info("using producer config {}", props);

        return props;
    }

    private ProducerFactory<String, M> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
