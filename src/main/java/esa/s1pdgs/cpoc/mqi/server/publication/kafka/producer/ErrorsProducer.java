package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Generic producer on a topic
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the type of the published message
 */
@Service
public class ErrorsProducer {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(ErrorsProducer.class);

    /**
     * Kafka properties
     */
    private final KafkaProperties properties;

    /**
     * Kafka template
     */
    private final KafkaTemplate<String, String> template;
    
    /**
     * Topic
     */
    private final String topic;

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    @Autowired
    public ErrorsProducer(final KafkaProperties properties) {
        this.properties = properties;
        this.topic = properties.getErrorTopic();
        this.template = new KafkaTemplate<>(producerFactory());
    }

    /**
     * Send a message to a topic and wait until one is published
     * 
     * @param descriptor
     */
    public boolean send(final String dto) {
        boolean ret = false;
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[send] Send object {}", dto);
            }
            template.send(topic, dto).get();
            ret = true;
        } catch (CancellationException | InterruptedException
                | ExecutionException e) {
            LOGGER.error("Cannot log error message in Kafka topic: {}", dto);
        }
        return ret;
    }

    /**
     * Producer configuration
     * 
     * @return
     */
    private Map<String, Object> producerConfigs() {
        Map<String, Object> props = new ConcurrentHashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG,
                properties.getProducer().getMaxRetries());
        return props;
    }

    private ProducerFactory<String, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
