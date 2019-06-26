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
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.errors.mqi.MqiPublicationError;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Generic producer on a topic
 * 
 * @author Viveris Technologies
 * @param <T>
 *            the type of the published message
 */
@Component
public class GenericProducer {

    private static final Logger LOGGER = LogManager.getLogger(GenericProducer.class);

    private final KafkaProperties properties;
    private final KafkaTemplate<String, Object> template;

    @Autowired
    public GenericProducer(final KafkaProperties properties) {
        this.properties = properties;
        this.template = new KafkaTemplate<>(producerFactory());
    }

    /**
     * Constructor for test
     * @param properties
     * @param template
     */
    protected GenericProducer(final KafkaProperties properties, final KafkaTemplate<String, Object> template) {
        this.properties = properties;
        this.template = template;
    }

    /**
     * Send a message to a topic and wait until one is published
     * 
     * @param descriptor
     */
    public void send(final String topic, final AbstractDto dto)
            throws MqiPublicationError {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[send] Send object {}", dto);
            }
            template.send(topic, dto).get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {
            throw new MqiPublicationError(topic, dto, dto.getProductName(), e.getMessage(), e);
        }
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
                JsonSerializer.class);
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        props.put(ProducerConfig.RETRIES_CONFIG,
                properties.getProducer().getMaxRetries());
        return props;
    }

    private ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
