package esa.s1pdgs.cpoc.mqi.server.service;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.message.ConsumptionController;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.config.ApplicationProperties;

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
     * Application properties
     */
    private final ApplicationProperties appProperties;
   
    
    private final MessagePersistence<T> messagePersistence;

    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;
    
    private final ConsumptionController consumptionController;

    final Map<ProductCategory, Map<String, Integer>> topicPriorities = new HashMap<>();

    @Autowired
    public MessageConsumptionController(
			final ApplicationProperties appProperties,
			final MessagePersistence<T> messagePersistence,
			final OtherApplicationService otherAppService,
            final ConsumptionController consumptionController
	) {
		this.appProperties = appProperties;
		this.messagePersistence = messagePersistence;
		this.otherAppService = otherAppService;
		this.consumptionController = consumptionController;


		appProperties.getProductCategories().forEach((cat, catProps) -> {
            topicPriorities.put(cat, new HashMap<>());
            catProps.getConsumption().getTopicsWithPriority().forEach((topic, priority) -> topicPriorities.get(cat).put(topic, priority));
        });

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
    	if (!topicPriorities.containsKey(category)) {
            throw new MqiCategoryNotAvailable(category, "consumer");
    	}
    	final GenericMessageDto<? extends AbstractMessage> message = nextMessageByCat(category);
        // if no message and consumer is pause => resume it
        if (message == null) {
            topicPriorities.get(category).keySet().forEach(consumptionController::resume);
        }
        return message;
    }

    final Comparator<AppCatMessageDto<? extends AbstractMessage>> priorityComparatorFor(final ProductCategory category) {
        return (o1, o2) -> {
            if (topicPriorities.get(category).get(o1.getTopic()) >
                    topicPriorities.get(category).get(o2.getTopic())) {
                return -1;
            } else if (topicPriorities.get(category).get(o1.getTopic())
                    .equals(topicPriorities.get(category).get(o2.getTopic()))) {
                if (o1.getCreationDate() == null) {
                    return 1;
                } else if (o2.getCreationDate() == null) {
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
        if (topicPriorities.containsKey(category)) {
            try {
                // Get resume details and topic
                message = messagePersistence.get(category, identifier);

                messagePersistence.ack(category, identifier, ack);

                // Get remaining message read
                nbReadingMsg = messagePersistence.getNbReadingMessages(message.getTopic(), appProperties.getHostname());
                ret = new ResumeDetails(message.getTopic(), message.getDto());
            } finally {
                // Resume consumer of concerned topic
                if (!stop && nbReadingMsg <= 0 && message != null) {
                    // Resume consumer
                    if (topicPriorities.get(category)
                            .containsKey(message.getTopic())) {
                        consumptionController.resume(message.getTopic());
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
