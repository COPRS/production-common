package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Generic producer on a topic
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the type of the published message
 */
public abstract class AbstractGenericProducer<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractGenericProducer.class);

    /**
     * Kafka properties
     */
    private final KafkaProperties properties;

    /**
     * Kafka template
     */
    private final KafkaTemplate<String, T> template;

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public AbstractGenericProducer(final KafkaProperties properties) {
        this.properties = properties;
        this.template = new KafkaTemplate<>(producerFactory());
    }

    /**
     * Constructor for test
     * @param properties
     * @param template
     */
    protected AbstractGenericProducer(final KafkaProperties properties,
            final KafkaTemplate<String, T> template) {
        this.properties = properties;
        this.template = template;
    }

    /**
     * Send a message to a topic and wait until one is published
     * 
     * @param descriptor
     */
    public void send(final String topic, final T dto)
            throws MqiPublicationError {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[send] Send object {}", dto);
            }
            template.send(topic, dto).get();
        } catch (CancellationException | InterruptedException
                | ExecutionException e) {
            throw new MqiPublicationError(topic, dto, extractProductName(dto),
                    e.getMessage(), e);
        }
    }

    /**
     * Extract the product name of the DTO
     * 
     * @param obj
     * @return
     */
    protected abstract String extractProductName(T obj);

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
                JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG,
                properties.getProducer().getMaxRetries());
        return props;
    }

    private ProducerFactory<String, T> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
