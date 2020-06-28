package esa.s1pdgs.cpoc.mqi.server.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryConsumptionProperties;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer.Factory;

/**
 * Manager of consumers
 * 
 * @author Viveris Technologies
 */
@Controller
public class MessageConsumptionController<T extends AbstractMessage> {

    /**
     * Logger
     */
    protected static final Logger LOGGER = LogManager.getLogger(MessageConsumptionController.class);

    /**
     * List of consumers
     */
    final Map<ProductCategory, Map<String, GenericConsumer<?>>> consumers;

    /**
     * Application properties
     */
    private final ApplicationProperties appProperties;
   
    
    private final MessagePersistence<T> messagePersistence;

    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;
    
    private final GenericConsumer.Factory<T> consumerFactory;
    
    MessageConsumptionController(
    		final Map<ProductCategory, Map<String, GenericConsumer<?>>> consumers,
			final ApplicationProperties appProperties, 
			final MessagePersistence<T> messagePersistence,
			final OtherApplicationService otherAppService,
			final Factory<T> consumerFactory
	) {
		this.consumers = consumers;
		this.appProperties = appProperties;
		this.messagePersistence = messagePersistence;
		this.otherAppService = otherAppService;
		this.consumerFactory = consumerFactory;
	}

	@Autowired
    public MessageConsumptionController(
            final ApplicationProperties appProperties,
            final KafkaProperties kafkaProperties,
            final MessagePersistence<T> messagePersistence,
            final OtherApplicationService otherAppService,
            final AppStatus appStatus) {
		this(
				new HashMap<>(), 
				appProperties,
                messagePersistence,
				otherAppService, 
				new GenericConsumer.Factory<>(kafkaProperties, messagePersistence, appStatus)
		);
    }

	/**
     * Start consumers according the configuration
     */
    @PostConstruct
    public void startConsumers() {	
        // Init the list of consumers
        for (final Map.Entry<ProductCategory,ProductCategoryProperties> catEntry : appProperties.getProductCategories().entrySet()) {
            final ProductCategory cat = catEntry.getKey();
            final ProductCategoryConsumptionProperties prop = catEntry.getValue().getConsumption();
            if (prop.isEnable()) {
                LOGGER.info("Creating consumers on topics {} with for category {}", prop.getTopicsWithPriority(),cat);
                final Map<String, GenericConsumer<?>> catConsumers = new HashMap<>();
                for (final Map.Entry<String, Integer> entry : prop.getTopicsWithPriority().entrySet()) {
                	final String topic = entry.getKey(); 
                	final int priority = entry.getValue();
                	LOGGER.debug("Creating new consumer with clientId {} on category {} (topic: {}) with priority {}", 
                			consumerFactory.clientIdForTopic(topic), cat, topic, priority);
                	catConsumers.put(topic, consumerFactory.newConsumerFor(cat, priority, topic));
                }
                consumers.put(cat, catConsumers);
            }
        }
        // Start the consumers
        for (final Map<String, GenericConsumer<?>> catConsumers : consumers.values()) {
            for (final GenericConsumer<?> consumer : catConsumers.values()) {
                LOGGER.info("Starting consumer on topic {}", consumer.getTopic());
                consumer.start();
            }
        }
    }

    /**
     * Get the next message for a given category
     * 
     * @param category category product category
     * @return next message for given category
     * @throws AbstractCodedException on error
     */
    public GenericMessageDto<? extends AbstractMessage> nextMessage(final ProductCategory category)
    		throws AbstractCodedException {
    	// invalid category
    	if (!consumers.containsKey(category)) {
            throw new MqiCategoryNotAvailable(category, "consumer");
    	}
    	final GenericMessageDto<? extends AbstractMessage> message = nextMessageByCat(category);
        // if no message and consumer is pause => resume it
        if (message == null) {
            for(final GenericConsumer<?> consumer : consumers.get(category).values()) {
                consumer.resume();
            }
        }
        return message;
    }
    
    final Comparator<AppCatMessageDto<? extends AbstractMessage>> priorityComparatorFor(final ProductCategory category)
    {
    	return (o1, o2) -> {
            if(consumers.get(category).get(o1.getTopic()).getPriority() >
                consumers.get(category).get(o2.getTopic()).getPriority()) {
                return -1;
            } else if(consumers.get(category).get(o1.getTopic()).getPriority() ==
                    consumers.get(category).get(o2.getTopic()).getPriority()) {
                if(o1.getCreationDate()==null) {
                    return 1;
                } else if(o2.getCreationDate()==null) {
                    return -1;
                } else {
                    return o1.getCreationDate().compareTo(o2.getCreationDate());
                }
            } else {
                return 1;
            }
        };
    }
    
    /**
     * Get the next message for auxiliary files
     * 
     * @return next message by category
     * @throws AbstractCodedException on error
     */
    @SuppressWarnings("unchecked")
    private GenericMessageDto<? extends AbstractMessage> nextMessageByCat(final ProductCategory category) throws AbstractCodedException {
        final List<AppCatMessageDto<T>> messages = messagePersistence.next(category, appProperties.getHostname());
        if (messages != null)
        {
            messages.sort(priorityComparatorFor(category));
            for (final AppCatMessageDto<? extends AbstractMessage> tmpMessage : messages) {
                if (send(category, messagePersistence, tmpMessage)) {
                    return (GenericMessageDto<? extends AbstractMessage>) convertToRestDto(tmpMessage);
                }
            }
        }
        return null;
    }

    /**
     * @param service app catalog mqi service
     * @throws AbstractCodedException on error
     */
    protected boolean send(final ProductCategory category, final MessagePersistence<T> service, final AppCatMessageDto<?> message)
    		throws AbstractCodedException {
        boolean ret;
        if (message.getState() == MessageState.SEND) {
            if (isSameSendingPod(message.getSendingPod())) {

                ret = service.send(category, message.getId(),
                        new AppCatSendMessageDto(appProperties.getHostname(),
                                false));
            } else {
                boolean isProcessing;
                try {
                    isProcessing = otherAppService.isProcessing(
                            message.getSendingPod(), category,
                            message.getId());
                } catch (final AbstractCodedException ace) {
                    isProcessing = false;
                    LOGGER.warn(
                            "{} No response from the other application, consider it as dead",
                            ace.getLogMessage());
                }
                if (!isProcessing) {
                    ret = service.send(category, message.getId(),
                            new AppCatSendMessageDto(appProperties.getHostname(),
                                    true));
                } else {
                    ret = false;
                }
            }
        } else {
            // We return this message after persisting it as sending
            ret = service.send(category, message.getId(),
                    new AppCatSendMessageDto(appProperties.getHostname(), false));
        }

        return ret;
    }

    /**
     * @param sendingPod sending pod
     */
    protected boolean isSameSendingPod(final String sendingPod) {
        return appProperties.getHostname().equals(sendingPod);
    }

    /**
     * Convert an app catalog rest object into mqi rest object from next API
     * 
     * @param object object to convert
     * @return converted message
     */
    private GenericMessageDto<?> convertToRestDto(
            final AppCatMessageDto<?> object) {
        if (object == null) {
            return null;
        }
        return new GenericMessageDto<>(object.getId(), object.getTopic(), object.getDto());
    }

    /**
     * Acknowledge a message
     * 
     * @param identifier identifier
     * @param ack acknowledge
     * @throws AbstractCodedException on error
     */
    public ResumeDetails ackMessage(final ProductCategory category,
            final long identifier, final Ack ack, final boolean stop)
            throws AbstractCodedException {
        ResumeDetails ret;
        int nbReadingMsg = 0;
        AppCatMessageDto<?> message = null;
        if (consumers.containsKey(category)) {
            try {
            	messagePersistence.ack(category, identifier, ack);
      
                // Get resume details and topic
                message = messagePersistence.get(category, identifier);

                // Get remaining message read
                nbReadingMsg = messagePersistence.getNbReadingMessages(message.getTopic(), appProperties.getHostname());
                ret = new ResumeDetails(message.getTopic(), message.getDto());
            } finally {
                // Resume consumer of concerned topic
                if (!stop && nbReadingMsg <= 0 && message != null) {
                    // Resume consumer
                    if (consumers.get(category)
                            .containsKey(message.getTopic())) {
                        consumers.get(category).get(message.getTopic())
                                .resume();
                    } else {
                        LOGGER.warn(
                                "[category {}] [messageCannot resume consumer for this topic because does not exist: {}",
                                category, message);
                    }
                }
            }
        } else {
            throw new MqiCategoryNotAvailable(category, "consumer");
        }
        return ret;
    }

}
