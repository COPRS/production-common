package esa.s1pdgs.cpoc.message.kafka.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.message.Consumption;
import esa.s1pdgs.cpoc.message.MessageConsumer;
import esa.s1pdgs.cpoc.message.MessageConsumerFactory;
import esa.s1pdgs.cpoc.message.kafka.KafkaAcknowledgement;
import esa.s1pdgs.cpoc.message.kafka.KafkaConsumption;
import esa.s1pdgs.cpoc.message.kafka.KafkaMessage;

@Component
@ConditionalOnProperty("kafka.consumer.group-id")
public class KafkaConsumptionController<M> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumptionController.class);

    private final MessageConsumerFactory<M> consumerFactory;
    private final KafkaProperties kafkaProperties;
    private final ConsumerRebalanceListener rebalanceListener;

    private final Map<String, ConcurrentMessageListenerContainer<String, M>> containers = new HashMap<>();

    @Autowired
    public KafkaConsumptionController(MessageConsumerFactory<M> consumerFactory, KafkaProperties kafkaProperties, @Nullable ConsumerRebalanceListener rebalanceListener) {
        this.consumerFactory = consumerFactory;
        this.kafkaProperties = kafkaProperties;
        this.rebalanceListener = rebalanceListener;
    }

    @PostConstruct
    public void initAndStartContainers() {
        List<MessageConsumer<M>> messageConsumers = consumerFactory.createConsumers();

        for (MessageConsumer<M> messageConsumer : messageConsumers) {
            containers.put(messageConsumer.topic(), containerFor(messageConsumer));
        }

        containers.forEach((topic, container) -> {
            LOG.info("Starting consumer on topic {}", topic);
            container.start();

        });
    }

    private ConcurrentMessageListenerContainer<String, M> containerFor(MessageConsumer<M> messageConsumer) {

        final String topic = messageConsumer.topic();

        KafkaConsumption<M> consumption = new KafkaConsumption<>();
        final ConcurrentMessageListenerContainer<String, M> container = new ConcurrentMessageListenerContainer<>(
                consumerFactory(topic, messageConsumer.messageType()),
                containerProperties(topic, listenerFor(messageConsumer, consumption))
        );
        consumption.setKafkaContainer(container);

        return container;
    }

    private MessageListener<String, M> listenerFor(MessageConsumer<M> messageConsumer, Consumption consumption) {

        return (AcknowledgingMessageListener<String, M>) (data, acknowledgment)
                -> messageConsumer.onMessage(new KafkaMessage<>(data.value(), data), new KafkaAcknowledgement(acknowledgment), consumption);
    }

    private <T> ConsumerFactory<String, T> consumerFactory(final String topic, final Class<T> dtoClass) {
        final JsonDeserializer<T> jsonDeserializer = new JsonDeserializer<>(dtoClass);
        jsonDeserializer.addTrustedPackages("*");
        final ErrorHandlingDeserializer<T> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        errorHandlingDeserializer.setFailedDeserializationFunction((failedDeserializationInfo) -> {
            LOG.error(
                    "Error on deserializing element from queue '{}'. Expected json of class {} but was: {}",
                    topic,
                    dtoClass.getName(),
                    new String(failedDeserializationInfo.getData())
            );
            return null;
        });

        return new DefaultKafkaConsumerFactory<>(
                consumerConfig(clientIdForTopic(topic)),
                new StringDeserializer(),
                errorHandlingDeserializer
        );
    }

    private Map<String, Object> consumerConfig(final String consumerId) {
        final Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaProperties.getConsumer().getGroupId());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaProperties.getConsumer().getMaxPollIntervalMs());
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaProperties.getConsumer().getMaxPollRecords());
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaProperties.getConsumer().getSessionTimeoutMs());
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaProperties.getConsumer().getHeartbeatIntvMs());
        props.put(ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, Collections.singletonList(RoundRobinAssignor.class));
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaProperties.getConsumer().getAutoOffsetReset());
        return props;
    }

    // use unique clientId to circumvent 'instance already exists' problem
    private String clientIdForTopic(final String topic) {
        return kafkaProperties.getClientId() + "-" +
                kafkaProperties.getHostname() + "-" +
                topic;
    }

    private ContainerProperties containerProperties(final String topic, final MessageListener<String, M> messageListener) {
        final ContainerProperties containerProp = new ContainerProperties(topic);
        containerProp.setMessageListener(messageListener);
        containerProp.setPollTimeout(kafkaProperties.getListener().getPollTimeoutMs());
        containerProp.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        if(rebalanceListener != null) {
            containerProp.setConsumerRebalanceListener(rebalanceListener);
        }

        return containerProp;
    }

    @PreDestroy
    public void stopContainers() {
        containers.forEach((topic, container) -> {
            LOG.info("Stopping consumer on topic {}", topic);
            container.stop();
        });
    }

}
