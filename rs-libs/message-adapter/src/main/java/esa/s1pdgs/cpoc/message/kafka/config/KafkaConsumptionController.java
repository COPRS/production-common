package esa.s1pdgs.cpoc.message.kafka.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.kafka.clients.consumer.ConsumerConfig;
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
import esa.s1pdgs.cpoc.message.ConsumptionController;
import esa.s1pdgs.cpoc.message.MessageConsumer;
import esa.s1pdgs.cpoc.message.MessageConsumerFactory;
import esa.s1pdgs.cpoc.message.kafka.ConsumptionConfigurationFactory;
import esa.s1pdgs.cpoc.message.kafka.ContainerPropertiesFactory;
import esa.s1pdgs.cpoc.message.kafka.KafkaAcknowledgement;
import esa.s1pdgs.cpoc.message.kafka.KafkaConsumerFactoryProvider;
import esa.s1pdgs.cpoc.message.kafka.KafkaConsumption;
import esa.s1pdgs.cpoc.message.kafka.KafkaMessage;

@Component
@ConditionalOnProperty("kafka.consumer.group-id")
public class KafkaConsumptionController<M> implements ConsumptionController {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumptionController.class);

    private static final Consumption NULL = new Consumption() {
        @Override
        public void pause() {
        }

        @Override
        public boolean isPaused() {
            //TODO what to return???
            return false;
        }

        @Override
        public void resume() {
        }
    };

    private final MessageConsumerFactory<M> consumerFactory;
    private final KafkaProperties kafkaProperties;
    private final ConsumptionConfigurationFactory consumptionConfigurationFactory;
    private final ContainerPropertiesFactory<M> containerPropertiesFactory;
    private final KafkaConsumerFactoryProvider<M> kafkaConsumerFactoryProvider;

    private final Map<String, ConcurrentMessageListenerContainer<String, M>> containers = new HashMap<>();

    private final Map<String, Consumption> consumptionControls = new ConcurrentHashMap<>();

    @Autowired
    public KafkaConsumptionController(MessageConsumerFactory<M> consumerFactory,
                                      KafkaProperties kafkaProperties,
                                      @Nullable ConsumptionConfigurationFactory consumptionConfigurationFactory,
                                      @Nullable ContainerPropertiesFactory<M> containerPropertiesFactory,
                                      @Nullable KafkaConsumerFactoryProvider<M> kafkaConsumerFactoryProvider) {
        this.consumerFactory = consumerFactory;
        this.kafkaProperties = kafkaProperties;
        this.consumptionConfigurationFactory = consumptionConfigurationFactory;
        this.containerPropertiesFactory = containerPropertiesFactory;
        this.kafkaConsumerFactoryProvider = kafkaConsumerFactoryProvider;
    }

    @PostConstruct
    public void initAndStartContainers() {
        List<MessageConsumer<M>> messageConsumers = consumerFactory.createConsumers();

        for (MessageConsumer<M> messageConsumer : messageConsumers) {
            ConcurrentMessageListenerContainer<String, M> container = containerFor(messageConsumer);
            containers.put(messageConsumer.topic(), container);

            KafkaConsumption<M> consumption = new KafkaConsumption<>();
            consumption.setKafkaContainer(container);
            consumptionControls.put(messageConsumer.topic(), consumption);
        }

        containers.forEach((topic, container) -> {
            LOG.info("Starting consumer on topic {}", topic);
            container.start();

        });
    }

    @Override
    public void pause(String topic) {
        consumptionControls.getOrDefault(topic, NULL).pause();
    }

    @Override
    public boolean isPaused(String topic) {
        return consumptionControls.getOrDefault(topic, NULL).isPaused();
    }

    @Override
    public void resume(String topic) {
        consumptionControls.getOrDefault(topic, NULL).resume();
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
                -> messageConsumer.onMessage(new KafkaMessage<>(data.value(), data), new KafkaAcknowledgement<>(acknowledgment, data), consumption);
    }

    private ConsumerFactory<String, M> consumerFactory(final String topic, final Class<M> dtoClass) {
        final JsonDeserializer<M> jsonDeserializer = new JsonDeserializer<>(dtoClass);
        jsonDeserializer.addTrustedPackages("*");
        final ErrorHandlingDeserializer<M> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(jsonDeserializer);

        errorHandlingDeserializer.setFailedDeserializationFunction((failedDeserializationInfo) -> {
            LOG.error(
                    "Error on deserializing element from queue '{}'. Expected json of class {} but was: {}",
                    topic,
                    dtoClass.getName(),
                    new String(failedDeserializationInfo.getData())
            );
            return null;
        });
        
        if(kafkaConsumerFactoryProvider != null) {
            return kafkaConsumerFactoryProvider.consumerFactoryFor(
                    consumerConfig(clientIdForTopic(topic)),
                    new StringDeserializer(),
                    errorHandlingDeserializer
            );
        }

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

        if(consumptionConfigurationFactory != null) {
            props.putAll(consumptionConfigurationFactory.consumptionConfiguration());
        }

        return props;
    }

    // use unique clientId to circumvent 'instance already exists' problem
    private String clientIdForTopic(final String topic) {
        return KafkaConsumerClientId
                .clientIdForRawIdAndTopic(kafkaProperties.getClientId() + kafkaProperties.getHostname(), topic);
    }

    private ContainerProperties containerProperties(final String topic, final MessageListener<String, M> messageListener) {


        if(containerPropertiesFactory != null) {
            return containerPropertiesFactory.containerPropertiesFor(topic, messageListener);
        }

        final ContainerProperties containerProp = new ContainerProperties(topic);
        containerProp.setMessageListener(messageListener);
        containerProp.setPollTimeout(kafkaProperties.getListener().getPollTimeoutMs());
        containerProp.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

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
