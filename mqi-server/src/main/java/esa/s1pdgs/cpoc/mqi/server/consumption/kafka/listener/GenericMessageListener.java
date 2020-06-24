package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.MessageConsumer;
import esa.s1pdgs.cpoc.mqi.server.service.AppCatalogMessagePersistence;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;
import esa.s1pdgs.cpoc.mqi.server.service.OtherApplicationService;

/**
 * Kafka message listener<br/>
 * Will poll a topic until consumed 1 message which can be processing by its
 * applications, then the consumer pauses<br/>
 * When a message is received, will check in applicative data if the message is
 * already processing or not. If not, the message is acknowledge when the
 * consumer pause. Else ask for the other application if it always processing this
 * message
 * 
 * @author Viveris Technologies
 * @param <T>
 */
public final class GenericMessageListener<T> implements AcknowledgingConsumerAwareMessageListener<String, T> {

    private static final Logger LOGGER = LogManager.getLogger(GenericMessageListener.class);

    private final KafkaProperties properties;
    private final MessagePersistence messagePersistence;
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
            final MessagePersistence messagePersistence,
            final OtherApplicationService otherAppService,
            final GenericConsumer<T> genericConsumer,
            final AppStatus appStatus,
            final MessageConsumer<T> additionalConsumer
    ) {
    	this.category = category;
        this.properties = properties;
        this.messagePersistence = messagePersistence;
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
        	    acknowledge(data, acknowledgment);
        	    return;
        	}
        	final T message = data.value();
        	LOGGER.debug("Handling message from kafka queue: {}", message);
        	
            // Save message
			messagePersistence.read(data, acknowledgment);
            appStatus.setWaiting();
        } catch (final Exception e) {        	
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
            appStatus.setError("MQI");
        }
    }

	/**
	 * Acknowledge KAFKA message
	 *
	 */
	final void acknowledge(final ConsumerRecord<String, T> data,
						   final Acknowledgment acknowledgment) {
		try {
			LOGGER.debug("Acknowledging KAFKA message: {}", data.value());
			acknowledgment.acknowledge();
		} catch (final Exception e) {
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
     * Pause the consumer
     */
    final void pause() {
        this.genericConsumer.pause();
    }
}
