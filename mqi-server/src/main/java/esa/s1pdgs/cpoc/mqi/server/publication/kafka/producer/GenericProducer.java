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
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;

/**
 * Generic producer on a topic
 * 
 * @author Viveris Technologies
 */
@Component
public class GenericProducer {

    private static final Logger LOGGER = LogManager.getLogger(GenericProducer.class);

    private final KafkaProperties properties;
    private final KafkaTemplate<String, Object> template;

    @Autowired
    public GenericProducer(final KafkaProperties properties) {
        this.properties = properties;
        LOGGER.debug("Generic producer config used: {}", this.properties);
        this.template = new KafkaTemplate<>(producerFactory());
    }

    /**
     * Constructor for test
     */
    protected GenericProducer(final KafkaProperties properties, final KafkaTemplate<String, Object> template) {
        this.properties = properties;
        this.template = template;
    }

    /**
     * Send a message to a topic and wait until one is published
     * 
     */
    public void send(final String topic, final AbstractMessage dto)
            throws MqiPublicationError {
        try {
            LOGGER.debug("Sending to '{}': {}", topic, dto);
            template.send(topic, dto).get();
        } catch (CancellationException | InterruptedException | ExecutionException e) {        	
        	if (dto instanceof ProductionEvent) {
        		final ProductionEvent event = (ProductionEvent) dto;
        		throw new MqiPublicationError(topic, dto, event.getProductName(), e.getMessage(), e);
        	}
        	throw new MqiPublicationError(topic, dto, "NOPRODUCTNAMEANYMORE", e.getMessage(), e);
        }
    }

    /**
     * Producer configuration
     * 
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
        return props;
    }

    private ProducerFactory<String, Object> producerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

}
