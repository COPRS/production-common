package esa.s1pdgs.cpoc.mqi.server.consumption;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryConsumptionProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer.Factory;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

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
    protected static final Logger LOGGER = LogManager.getLogger(MessageConsumptionController.class);

    /**
     * List of consumers
     */
    final Map<ProductCategory, Map<String, GenericConsumer<?>>> consumers;

    /**
     * Application properties
     */
    private final ApplicationProperties appProperties;
   
    
    private final AppCatalogMqiService service;

    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;
    
    private final GenericConsumer.Factory consumerFactory;
    
    MessageConsumptionController(
    		Map<ProductCategory, Map<String, GenericConsumer<?>>> consumers,
			ApplicationProperties appProperties, 
			AppCatalogMqiService service, 
			OtherApplicationService otherAppService,
			Factory consumerFactory
	) {
		this.consumers = consumers;
		this.appProperties = appProperties;
		this.service = service;
		this.otherAppService = otherAppService;
		this.consumerFactory = consumerFactory;
	}

	@Autowired
    public MessageConsumptionController(
            final ApplicationProperties appProperties,
            final KafkaProperties kafkaProperties,
            final AppCatalogMqiService service,	
            final OtherApplicationService otherAppService,
            final AppStatus appStatus) {
		this(
				new HashMap<>(), 
				appProperties, 
				service, 
				otherAppService, 
				new GenericConsumer.Factory(kafkaProperties, service, otherAppService, appStatus)
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
                	final int prio = entry.getValue();
                	catConsumers.put(topic, consumerFactory.newConsumerFor(cat, prio, topic));
                }
                consumers.put(cat, catConsumers);
            }
        }
        // Start the consumers
        for (final Map<String, GenericConsumer<?>> catConsumers : consumers.values()) {
            for (GenericConsumer<?> consumer : catConsumers.values()) {
                LOGGER.info("Starting consumer on topic {}", consumer.getTopic());
                consumer.start();
            }
        }
    }

    /**
     * Get the next message for a given category
     * 
     * @param category
     * @return
     * @throws AbstractCodedException
     */
    public GenericMessageDto<? extends AbstractDto> nextMessage(final ProductCategory category)
    		throws AbstractCodedException {
    	// invalid category
    	if (!consumers.containsKey(category)) {
            throw new MqiCategoryNotAvailable(category, "consumer");
    	}
    	final GenericMessageDto<? extends AbstractDto> message = nextMessageByCat(category);
        // if no message and consumer is pause => resume it
        if (message == null) {
            for(final GenericConsumer<?> consumer : consumers.get(category).values()) {
                consumer.resume();
            }
        }
        return message;
    }
    
    private final Comparator<AppCatMessageDto<? extends AbstractDto>> priorityComparatorFor(final ProductCategory category)
    {
    	return new Comparator<AppCatMessageDto<? extends AbstractDto>>() {
            @Override
            public int compare(AppCatMessageDto<? extends AbstractDto> o1, AppCatMessageDto<? extends AbstractDto> o2) {
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
            }                
        };
    }
    
    /**
     * Get the next message for auxiliary files
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    private final GenericMessageDto<? extends AbstractDto> nextMessageByCat(final ProductCategory category) throws AbstractCodedException {
        final List<AppCatMessageDto<? extends AbstractDto>> messages = service.next(category, appProperties.getHostname());
        if (messages != null)
        {
            Collections.sort(messages, priorityComparatorFor(category));
            for (final AppCatMessageDto<? extends AbstractDto> tmpMessage : messages) {
                if (send(category, service, tmpMessage)) {
                    return (GenericMessageDto<? extends AbstractDto>) convertToRestDto(tmpMessage);
                }
            }
        }
        return null;
    }

    /**
     * @param service
     * @param messageId
     * @param force
     * @return
     * @throws AbstractCodedException
     */
    protected boolean send(final ProductCategory category, final AppCatalogMqiService service, final AppCatMessageDto<?> message) 
    		throws AbstractCodedException {
        boolean ret = false;
        if (message.getState() == MessageState.SEND) {
            if (isSameSendingPod(message.getSendingPod())) {

                ret = service.send(category, message.getIdentifier(),
                        new AppCatSendMessageDto(appProperties.getHostname(),
                                false));
            } else {
                boolean isProcessing = false;
                try {
                    isProcessing = otherAppService.isProcessing(
                            message.getSendingPod(), category,
                            message.getIdentifier());
                } catch (AbstractCodedException ace) {
                    isProcessing = false;
                    LOGGER.warn(
                            "{} No response from the other application, consider it as dead",
                            ace.getLogMessage());
                }
                if (!isProcessing) {
                    ret = service.send(category, message.getIdentifier(),
                            new AppCatSendMessageDto(appProperties.getHostname(),
                                    true));
                } else {
                    ret = false;
                }
            }
        } else {
            // We return this message after persisting it as sending
            ret = service.send(category, message.getIdentifier(),
                    new AppCatSendMessageDto(appProperties.getHostname(), false));
        }

        return ret;
    }

    /**
     * @param sendingPod
     * @return
     */
    protected boolean isSameSendingPod(final String sendingPod) {
        return appProperties.getHostname().equals(sendingPod);
    }

    /**
     * Convert an app catalog rest object into mqi rest object from next API
     * 
     * @param object
     * @return
     */
    private GenericMessageDto<?> convertToRestDto(
            final AppCatMessageDto<?> object) {
        if (object == null) {
            return null;
        }
        return new GenericMessageDto<>(object.getIdentifier(), object.getTopic(), object.getDto());
    }

    /**
     * Acknowledge a message
     * 
     * @param identifier
     * @param ack
     * @param message
     * @throws AbstractCodedException
     */
    public ResumeDetails ackMessage(final ProductCategory category,
            final long identifier, final Ack ack, final boolean stop)
            throws AbstractCodedException {
        ResumeDetails ret = null;
        int nbReadingMsg = 0;
        AppCatMessageDto<?> message = null;
        if (consumers.containsKey(category)) {
            try {
            	service.ack(category, identifier, ack);
      
                // Get resume details and topic
                message = service.get(category, identifier);

                // Get remaining message read
                nbReadingMsg = service.getNbReadingMessages(message.getTopic(), appProperties.getHostname());
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
