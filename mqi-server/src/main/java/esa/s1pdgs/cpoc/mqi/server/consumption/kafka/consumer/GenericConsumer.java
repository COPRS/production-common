package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer.AckMode;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener.GenericMessageListener;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener.MemoryConsumerAwareRebalanceListener;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

/**
 * Generic consumer
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public class GenericConsumer<T> {

    /**
     * Properties
     */
    private final KafkaProperties properties;

    /**
     * Service for persisting data
     */
    private final GenericAppCatalogMqiService<T> service;

    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;

    /**
     * Application status
     */
    private final AppStatus appStatus;

    /**
     * Topic name
     */
    private final String topic;
    
    /**
     * Topic priority
     */
    private final int priority;

    /**
     * Listener container
     */
    private ConcurrentMessageListenerContainer<String, T> container;

    /**
     * 
     */
    private final Class<T> consumedMsgClass;

    /**
     * 
     * @param properties
     * @param service
     * @param otherAppService
     * @param appStatus
     * @param topic
     * @param consumedMsgClass
     */
    public GenericConsumer(final KafkaProperties properties,
            final GenericAppCatalogMqiService<T> service,
            final OtherApplicationService otherAppService,
            final AppStatus appStatus, final String topic,
            final int priority,
            final Class<T> consumedMsgClass) {
        this.properties = properties;
        this.service = service;
        this.otherAppService = otherAppService;
        this.appStatus = appStatus;
        this.topic = topic;
        this.priority = priority;
        this.consumedMsgClass = consumedMsgClass;
    }

    /**
     * @return the topic
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
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
                new GenericMessageListener<>(properties, service,
                        otherAppService, this, appStatus);

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
        containerProp.setAckMode(AckMode.MANUAL_IMMEDIATE);
        containerProp.setConsumerRebalanceListener(
                new MemoryConsumerAwareRebalanceListener(service,
                        properties.getConsumer().getGroupId(),
                        properties.getConsumer().getOffsetDftMode()));
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
                Collections.singletonList(RoundRobinAssignor.class));
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, properties.getClientId());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                properties.getConsumer().getAutoOffsetReset());
        return props;
    }
}
