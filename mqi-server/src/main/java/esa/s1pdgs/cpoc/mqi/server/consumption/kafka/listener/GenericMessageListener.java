package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.MessageConsumer;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

/**
 * Kafka message listener<br/>
 * Will poll a topic until consumed 1 message which can be processing by its
 * applications, then the consumer pauses<br/>
 * When a message is received, will check in applicative data if the message is
 * already processing or not. If not, the message is acknowledge when the
 * consumer pause. Else ask for the other application if it always prossing this
 * message
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public final class GenericMessageListener<T> implements AcknowledgingConsumerAwareMessageListener<String, T> {

    private static final Logger LOGGER = LogManager.getLogger(GenericMessageListener.class);

    private final KafkaProperties properties;
    private final AppCatalogMqiService service;
    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;
    private final GenericConsumer<T> genericConsumer;
    private final AppStatus appStatus;    
    private final ProductCategory category;
    private final MessageConsumer<T> additionalConsumer;

    public GenericMessageListener(
    		final ProductCategory category,
    		final KafkaProperties properties,
            final AppCatalogMqiService service,
            final OtherApplicationService otherAppService,
            final GenericConsumer<T> genericConsumer,
            final AppStatus appStatus,
            final MessageConsumer<T> additionalConsumer
    ) {
    	this.category = category;
        this.properties = properties;
        this.service = service;
        this.otherAppService = otherAppService;
        this.genericConsumer = genericConsumer;
        this.appStatus = appStatus;
        this.additionalConsumer = additionalConsumer;
    }
    


    /**
     * Listener. Method call when a message is received
     */
    @Override
    public void onMessage(
    		final ConsumerRecord<String, T> data,
            final Acknowledgment acknowledgment,
            final Consumer<?, ?> consumer
    ) {

        try {
        	// handle invalid message type
        	if (!category.getDtoClass().isAssignableFrom(data.value().getClass())) {
        		LOGGER.debug("Invalid message type '{}' detected: {}", data.value().getClass().getName(), data.value());
        	    acknowlegde(data, acknowledgment);
        	    return;
        	}
        	final T message = data.value();
        	LOGGER.debug("Handling message from kafka queue: {}", message);
        	
            // Save message
			final AppCatMessageDto<T> result = saveInAppCat(data, false);        
        	additionalConsumer.consume(message);
            handleMessage(data, acknowledgment, result);
            appStatus.resetError();
        } catch (Exception e) {        	
        	if (e instanceof AbstractCodedException) {
        		final AbstractCodedException ace = (AbstractCodedException) e;
        		LOGGER.error(
        				"{} we cannot acknowledge this message and try the next time; Set app status in error",
        				ace.getLogMessage()
        		);
        	}
        	else {
        		LOGGER.error(LogUtils.toString(e));
        	}         
            appStatus.setError();
        }
    }

	final void handleMessage(
			final ConsumerRecord<String, T> data, 
			final Acknowledgment acknowledgment,
			final AppCatMessageDto<T> result
	) throws AbstractCodedException {
	   	final T message = data.value();
	   	
		// Deal with result
		switch (result.getState()) {
		    case ACK_KO:
		    case ACK_OK:
		    case ACK_WARN:	
		        LOGGER.debug("Message ignored and going to the next (state is ACK_WARN): {}", message);	
		        acknowlegde(data, acknowledgment);
		        break;
		    case SEND:
		    	LOGGER.debug("Message {} is already processing (state is SEND)", result.getId());	
		        if (properties.getHostname().equals(result.getSendingPod())) {
			        LOGGER.debug("Message {} already processed by this pod (state is SEND). Ignoring and pausing consumption", 
			        		result.getId());	
		            acknowlegde(data, acknowledgment);
		            pause();
		        } else {
		            // Message processing by another pod
		            if (messageShallBeIgnored(data, result)) {
		            	LOGGER.debug("Message {} shall be ignored (state is SEND). Ignoring...", result.getId());
		                acknowlegde(data, acknowledgment);
		            } else {
		            	LOGGER.debug("Forced message {} transition from SEND to READ). Pausing consumption", 
		            			result.getId());
		                // We have forced the reading
		                acknowlegde(data, acknowledgment);
		                pause();
		            }
		        }
		        break;
		    default:
		        // Message assigned
		    	LOGGER.debug("Message {} assigned to this pod. Pausing consumption ...", result.getId());
		        acknowlegde(data, acknowledgment);
		        pause();
		        break;
		}
	}

    /**
     * Pause the consumer
     */
    final void pause() {
        this.genericConsumer.pause();
    }

    /**
     * Acknowledge KAFKA message
     * 
     * @param data
     * @param acknowledgment
     */
    final void acknowlegde(final ConsumerRecord<String, T> data,
            final Acknowledgment acknowledgment) {
        try {
        	LOGGER.debug("Acknowledging KAFKA message: {}", data.value());
            acknowledgment.acknowledge();
        } catch (Exception e) {
        	LOGGER.error(
        			"Error on acknowledging KAFKA message (topic: {}, partition: {}, offset: {}) {} : {}", 
        			data.topic(),
        			data.partition(), 
        			data.offset(),
        			data.value(),
        			LogUtils.toString(e)
        	);
        }
    }

    /**
     * true if the message shall be ignored, false else
     * 
     * @param lightMessage
     * @return
     * @throws AbstractCodedException
     */
    final boolean messageShallBeIgnored(
            final ConsumerRecord<String, T> data,
            final AppCatMessageDto<T> mess)
            throws AbstractCodedException {
        boolean ret = false;
        // Ask to the other application
        try {
            ret = otherAppService.isProcessing(mess.getSendingPod(), category, mess.getId());
        } catch (AbstractCodedException ace) {
            ret = false;
            LOGGER.warn("{} No response from the other application, consider it as dead", ace.getLogMessage());
        }
        if (!ret) {
        	LOGGER.debug("No other pod is handling the message {}. Enforcing update to state READ...", mess.getId());
			final AppCatMessageDto<T> resultForce = saveInAppCat(data, true);
            if (resultForce.getState() != MessageState.READ) {
                ret = true;
            }
            // no idea what this message shall mean - leave it here for historical purposes and someone understanding
            LOGGER.warn("We force the reading for the message {}, will the message be ignored {}", mess, ret);
        }
        else {
        	LOGGER.info("Message {} is already handled by other pod", mess.getId());
        }
        return ret;
    }
    
    @SuppressWarnings("unchecked")
	private final AppCatMessageDto<T> saveInAppCat(final ConsumerRecord<String, T> data, boolean force) throws AbstractCodedException {
    	return (AppCatMessageDto<T>) service.read(
    			category,
    			data.topic(),
                data.partition(), 
                data.offset(),
                new AppCatReadMessageDto<T>(
                        properties.getConsumer().getGroupId(),
                        properties.getHostname(), 
                        force, 
                        data.value()
                )
        );       
    }
}
