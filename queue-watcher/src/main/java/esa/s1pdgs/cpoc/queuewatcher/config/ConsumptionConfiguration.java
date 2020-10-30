package esa.s1pdgs.cpoc.queuewatcher.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import esa.s1pdgs.cpoc.message.Acknowledgement;
import esa.s1pdgs.cpoc.message.Consumption;
import esa.s1pdgs.cpoc.message.kafka.ConsumptionConfigurationFactory;
import esa.s1pdgs.cpoc.message.Message;
import esa.s1pdgs.cpoc.message.MessageConsumer;
import esa.s1pdgs.cpoc.message.MessageConsumerFactory;
import esa.s1pdgs.cpoc.message.kafka.KafkaConsumerFactoryProvider;

@Configuration
public class ConsumptionConfiguration<M> {

    private static final Logger LOG = LoggerFactory.getLogger(ConsumptionConfiguration.class);

    @Bean
    public MessageConsumerFactory<String> messageConsumerFactory(final ApplicationProperties applicationProperties) {
        final List<MessageConsumer<String>> consumers
                = applicationProperties.getKafkaTopics()
                .stream().map(topic -> consumerFor(topic, applicationProperties))
                .collect(Collectors.toList());

        return () -> consumers;
    }

    @Bean
    public ConsumptionConfigurationFactory consumptionConfigurationFactory() {
        final Map<String, Object> additionalProps = new HashMap<>();
        additionalProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return () -> additionalProps;
    }

    @Bean
    public KafkaConsumerFactoryProvider<M> kafkaConsumerFactoryProvider() {
        return (configs, keyDeserializer, valueDeserializer) -> new DefaultKafkaConsumerFactory<>(configs);
    }

    private MessageConsumer<String> consumerFor(final String topic, final ApplicationProperties properties) {

        return new MessageConsumer<String>() {

            @Override
            public void onMessage(Message<String> message, Acknowledgement acknowledgement, Consumption consumption) {
                // do something with received record
                LOG.debug("Received message from topic {}: {} ", topic, message.data());

                String fileName = properties.getKafkaFolder() + "/kafka-" + topic + ".log";

                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName), true))) {
                    writer.write(message.data() + "\n");
                } catch (IOException ex) {
                    LOG.error("An IO error occurred while accessing {}", fileName);
                }
            }

            @Override
            public Class<String> messageType() {
                return String.class;
            }

            @Override
            public String topic() {
                return topic;
            }
        };
    }

}
