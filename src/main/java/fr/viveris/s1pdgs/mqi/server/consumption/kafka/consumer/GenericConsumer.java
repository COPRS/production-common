package fr.viveris.s1pdgs.mqi.server.consumption.kafka.consumer;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.github.grantneale.kafka.LagBasedPartitionAssignor;

import fr.viveris.s1pdgs.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;
import fr.viveris.s1pdgs.mqi.server.consumption.kafka.listener.MemoryConsumerAwareRebalanceListener;

/**
 * Generic consumer
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public class GenericConsumer<T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(GenericConsumer.class);

    /**
     * Properties
     */
    private final KafkaProperties properties;

    /**
     * Topic name
     */
    private final String topic;

    /**
     * Last consumed message
     */
    private GenericMessageDto<T> consumedMessage;

    /**
     * Integer to build the message identifier
     */
    private final AtomicInteger inc;

    /**
     * Listener container
     */
    private ConcurrentMessageListenerContainer<String, T> container;

    /**
     * 
     */
    private final Class<T> consumedMsgClass;

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public GenericConsumer(final KafkaProperties properties, final String topic,
            final Class<T> consumedMsgClass) {
        this.properties = properties;
        this.topic = topic;
        this.consumedMsgClass = consumedMsgClass;
        this.inc = new AtomicInteger(0);
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the consumedMessage
     */
    public GenericMessageDto<T> getConsumedMessage() {
        return consumedMessage;
    }

    /**
     * @param consumedMessage
     *            the consumedMessage to set
     */
    public void setConsumedMessage(final GenericMessageDto<T> consumedMessage) {
        this.consumedMessage = consumedMessage;
    }

    /**
     * @return the consumedMsgClass
     */
    public Class<T> getConsumedMsgClass() {
        return consumedMsgClass;
    }

    /**
     * Start the consumer
     */
    public void start() {
        AcknowledgingConsumerAwareMessageListener<String, T> messageListener =
                new AcknowledgingConsumerAwareMessageListener<String, T>() {
                    @Override
                    public void onMessage(ConsumerRecord<String, T> data,
                            Acknowledgment acknowledgment,
                            Consumer<?, ?> consumer) {
                        // Get message
                        long identifier =
                                Objects.hash(topic, inc.incrementAndGet());
                        consumedMessage = new GenericMessageDto<T>(identifier,
                                data.topic(), data.value());

                        // Ack
                        try {
                            acknowledgment.acknowledge();
                        } catch (Exception exc) {
                            LOGGER.error(
                                    "[topic {}] [partition {}] [offset {}] Cannot ack KAFKA message: {}",
                                    topic, data.partition(), data.offset(),
                                    exc.getMessage());
                        }

                        // Pause
                        pause();
                    }
                };

        container = new ConcurrentMessageListenerContainer<>(consumerFactory(),
                containerProperties(topic, messageListener));

        container.start();
    }

    /**
     * Resume the consumer
     */
    public void resume() {
        container.resume();
    }

    /**
     * Pause the consumer
     */
    public void pause() {
        container.pause();
    }

    /**
     * Return true if the container is paused
     */
    public boolean isPaused() {
        return container.isContainerPaused();
    }

    /**
     * Build the consumer factory
     * 
     * @return
     */
    private ConsumerFactory<String, T> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfig(),
                new StringDeserializer(),
                new JsonDeserializer<>(consumedMsgClass));
    }

    /**
     * Build the container properties
     * 
     * @param topic
     * @param messageListener
     * @return
     */
    private ContainerProperties containerProperties(final String topic,
            final MessageListener<String, T> messageListener) {
        ContainerProperties containerProp = new ContainerProperties(topic);
        containerProp.setMessageListener(messageListener);
        containerProp
                .setPollTimeout(properties.getListener().getPollTimeoutMs());
        containerProp.setAckMode(
                AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
        containerProp.setConsumerRebalanceListener(
                new MemoryConsumerAwareRebalanceListener());
        return containerProp;
    }

    /**
     * Consumer configuration
     * 
     * @return
     */
    private Map<String, Object> consumerConfig() {
        Map<String, Object> props = new ConcurrentHashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                properties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                properties.getConsumer().getGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG,
                properties.getConsumer().getMaxPollIntervalMs());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,
                properties.getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG,
                properties.getConsumer().getSessionTimeoutMs());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG,
                properties.getConsumer().getHeartbeatIntvMs());
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
                Collections.singletonList(LagBasedPartitionAssignor.class));
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, properties.getClientId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                properties.getConsumer().getAutoOffsetReset());
        return props;
    }
}
