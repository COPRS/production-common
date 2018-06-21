package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.KafkaTemplate;

import fr.viveris.s1pdgs.level0.wrapper.model.exception.KafkaSendException;

public abstract class AbstractGenericProducer<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(AbstractGenericProducer.class);

    /**
     * KAFKA template
     */
    private final KafkaTemplate<String, T> kafkaTemplate;

    /**
     * Name of the topic
     */
    private final String topic;

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param kafkaTopic
     */
    public AbstractGenericProducer(final KafkaTemplate<String, T> kafkaTemplate,
            final String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    /**
     * Send a message to a topic and wait until one is published
     * 
     * @param descriptor
     */
    protected void send(final T dto) throws KafkaSendException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[send] Send object {}", dto);
            }
            kafkaTemplate.send(topic, dto).get();
        } catch (CancellationException | InterruptedException
                | ExecutionException e) {
            throw new KafkaSendException(topic, dto, extractProductName(dto),
                    e.getMessage(), e);
        }
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * Extract the product name of the DTO
     * 
     * @param obj
     * @return
     */
    protected abstract String extractProductName(T obj);

}
