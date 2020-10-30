package esa.s1pdgs.cpoc.mqi.server.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.ContainerProperties;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.message.MessageConsumer;
import esa.s1pdgs.cpoc.message.MessageConsumerFactory;
import esa.s1pdgs.cpoc.message.kafka.ContainerPropertiesFactory;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.server.consumption.GenericMessageListener;
import esa.s1pdgs.cpoc.mqi.server.consumption.MemoryConsumerAwareRebalanceListener;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;

@Configuration
public class KafkaConfig<M extends AbstractMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaConfig.class);

    @Bean
    public ContainerPropertiesFactory<M> conainerPropertiesFactory(final KafkaProperties kafkaProperties, final MessagePersistence<M> messagePersistence) {
        return (topic, messageListener) -> {
            {
                final ContainerProperties containerProp = new ContainerProperties(topic);
                containerProp.setMessageListener(messageListener);
                containerProp.setPollTimeout(kafkaProperties.getListener().getPollTimeoutMs());
                containerProp.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
                containerProp.setConsumerRebalanceListener(
                        new MemoryConsumerAwareRebalanceListener<>(
                                messagePersistence,
                                kafkaProperties.getConsumer().getGroupId(),
                                kafkaProperties.getConsumer().getOffsetDftMode()
                        )
                );
                return containerProp;
            }
        };
    }

    @Bean
    public MessageConsumerFactory<M> consumerFactory(final MessagePersistence<M> messagePersistence, final AppStatus appStatus, final ApplicationProperties appProperties) {
        return () -> {

             final List<MessageConsumer<M>> consumers = new ArrayList<>();

                // Init the list of consumers
                for (final Map.Entry<ProductCategory, ApplicationProperties.ProductCategoryProperties> catEntry : appProperties.getProductCategories().entrySet()) {
                    final ProductCategory cat = catEntry.getKey();
                    final ApplicationProperties.ProductCategoryConsumptionProperties prop = catEntry.getValue().getConsumption();
                    if (prop.isEnable()) {
                        LOG.info("Creating consumers on topics {} with for category {}", prop.getTopicsWithPriority(),cat);
                        for (final Map.Entry<String, Integer> entry : prop.getTopicsWithPriority().entrySet()) {
                            final String topic = entry.getKey();
                            final int priority = entry.getValue();
                            LOG.info("Creating new consumer on category {} (topic: {} class: {}) with priority {}",
                                    cat,
                                    topic,
                                    cat.getDtoClass().getSimpleName(),
                                    priority);



                            consumers.add(new GenericMessageListener<>(cat, messagePersistence, appStatus, topic, cat.getDtoClass()));
                        }
                    }
                }

            return consumers;
        };
    }

}
