package fr.viveris.s1pdgs.mqi.server.consumption;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties.ProductCategoryConsumptionProperties;
import fr.viveris.s1pdgs.mqi.server.ApplicationProperties.ProductCategoryProperties;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;
import fr.viveris.s1pdgs.mqi.server.consumption.kafka.consumer.GenericConsumer;

/**
 * Manager of consumers
 * 
 * @author Viveris Technologies
 */
@Controller
public class MessageConsumptionController {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(MessageConsumptionController.class);

    /**
     * List of consumers
     */
    protected final Map<ProductCategory, GenericConsumer<?>> consumers;

    /**
     * Application properties
     */
    private final ApplicationProperties appProperties;

    /**
     * Kafka properties
     */
    private final KafkaProperties kafkaProperties;

    /**
     * Constructor
     * 
     * @param appProperties
     * @param kafkaProperties
     */
    @Autowired
    public MessageConsumptionController(
            final ApplicationProperties appProperties,
            final KafkaProperties kafkaProperties) {
        this.consumers = new HashMap<>();
        this.appProperties = appProperties;
        this.kafkaProperties = kafkaProperties;
    }

    /**
     * Start consumers according the configuration
     */
    @PostConstruct
    public void startConsumers() {

        // Init the list of consumers
        for (ProductCategory cat : appProperties.getProductCategories()
                .keySet()) {
            ProductCategoryProperties catProp =
                    appProperties.getProductCategories().get(cat);
            ProductCategoryConsumptionProperties prop =
                    catProp.getConsumption();
            if (prop.isEnable()) {
                LOGGER.info(
                        "Creating consumer on topic {} with for category {}",
                        prop.getTopics(), cat);
                switch (cat) {
                    case AUXILIARY_FILES:
                        consumers.put(cat,
                                new GenericConsumer<AuxiliaryFileDto>(
                                        kafkaProperties, prop.getTopics(),
                                        AuxiliaryFileDto.class));
                        break;
                    case EDRS_SESSIONS:
                        consumers.put(cat,
                                new GenericConsumer<EdrsSessionDto>(
                                        kafkaProperties, prop.getTopics(),
                                        EdrsSessionDto.class));
                        break;
                    case LEVEL_JOBS:
                        consumers.put(cat,
                                new GenericConsumer<LevelJobDto>(
                                        kafkaProperties, prop.getTopics(),
                                        LevelJobDto.class));
                        break;
                    case LEVEL_PRODUCTS:
                        consumers.put(cat,
                                new GenericConsumer<LevelProductDto>(
                                        kafkaProperties, prop.getTopics(),
                                        LevelProductDto.class));
                        break;
                    case LEVEL_REPORTS:
                        consumers.put(cat,
                                new GenericConsumer<LevelReportDto>(
                                        kafkaProperties, prop.getTopics(),
                                        LevelReportDto.class));
                        break;
                }
            }
        }
        // Start the consumers
        for (GenericConsumer<?> consumer : consumers.values()) {
            LOGGER.info("Starting consumer on topic {}", consumer.getTopic());
            consumer.start();
        }
    }

    /**
     * Get the next message for a given category
     * 
     * @param category
     * @return
     * @throws MqiCategoryNotAvailable
     */
    public GenericMessageDto<?> nextMessage(final ProductCategory category)
            throws MqiCategoryNotAvailable {
        GenericMessageDto<?> message = null;
        if (consumers.containsKey(category)) {
            message = (GenericMessageDto<?>) consumers.get(category)
                    .getConsumedMessage();
        } else {
            throw new MqiCategoryNotAvailable(category, "consumer");
        }
        return message;
    }

    /**
     * Acknowledge a message
     * 
     * @param identifier
     * @param ack
     * @param message
     * @throws MqiCategoryNotAvailable
     */
    public boolean ackMessage(final ProductCategory category,
            final long identifier, final Ack ack)
            throws MqiCategoryNotAvailable {
        boolean result = false;
        if (consumers.containsKey(category)) {
            // Retrieve message
            GenericMessageDto<?> ackMsg = (GenericMessageDto<?>) consumers
                    .get(category).getConsumedMessage();
            if (ackMsg != null && ackMsg.getIdentifier() == identifier) {
                consumers.get(category).setConsumedMessage(null);

                // Resume consumer
                consumers.get(category).resume();
                result = true;
            }
        } else {
            throw new MqiCategoryNotAvailable(category, "consumer");
        }
        return result;
    }

}
