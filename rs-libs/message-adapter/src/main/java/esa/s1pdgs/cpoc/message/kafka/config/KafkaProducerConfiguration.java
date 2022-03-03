package esa.s1pdgs.cpoc.message.kafka.config;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.lang.Nullable;

import esa.s1pdgs.cpoc.message.MessageProducer;
import esa.s1pdgs.cpoc.message.kafka.KafkaMessageProducer;
import esa.s1pdgs.cpoc.message.kafka.LagBasedPartitioner;
import esa.s1pdgs.cpoc.message.kafka.PartitionLagFetcher;
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
    @ConditionalOnProperty("kafka.producer.max-retries")
    public MessageProducer<M> messageProducer() {
        return new KafkaMessageProducer<>(new KafkaTemplate<>(producerFactory()));
    }

    /**
     * Producer configuration
     */
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

        if (producerConfigurationFactory != null) {
            props.putAll(producerConfigurationFactory.producerConfiguration());
        }

        if (properties.getProducer().getLagBasedPartitioner() != null) {
            LOG.info("using lag based partitioner");
            props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, LagBasedPartitioner.class);
            props.put(LagBasedPartitioner.KAFKA_PROPERTIES, properties);

            props.put(LagBasedPartitioner.PARTITION_LAG_FETCHER_SUPPLIER, (Supplier<PartitionLagFetcher>) () -> {
                Map<String, Object> adminConfig = new HashMap<>();
                adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
                return new PartitionLagFetcher(Admin.create(adminConfig), properties);
            });
        }

        LOG.info("using producer config {}", props);

        return props;
    }

    private ProducerFactory<String, M> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
