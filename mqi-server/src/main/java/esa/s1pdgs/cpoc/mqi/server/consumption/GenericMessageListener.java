package esa.s1pdgs.cpoc.mqi.server.consumption;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.message.Acknowledgement;
import esa.s1pdgs.cpoc.message.Consumption;
import esa.s1pdgs.cpoc.message.Message;
import esa.s1pdgs.cpoc.message.MessageConsumer;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.server.service.MessagePersistence;

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
public final class GenericMessageListener<T extends AbstractMessage> implements MessageConsumer<T> {

    private static final Logger LOGGER = LogManager.getLogger(GenericMessageListener.class);

	private final MessagePersistence<T> messagePersistence;
    private final AppStatus appStatus;
    private final ProductCategory category;
    private final String topic;
    private final Class<T> messageType;

	public GenericMessageListener(
    		final ProductCategory category,
            final MessagePersistence<T> messagePersistence,
            final AppStatus appStatus,
			final String topic,
			final Class<T> messageType
    ) {
    	this.category = category;
		this.messagePersistence = messagePersistence;
        this.appStatus = appStatus;
        this.topic = topic;
        this.messageType = messageType;
	}


    /**
     * Listener. Method call when a message is received
     */
    @Override
    public void onMessage(
			final Message<T> message,
			final Acknowledgement acknowledgment,
			final Consumption consumption
    ) {

        try {
        	// handle invalid message type
        	if (!category.getDtoClass().isAssignableFrom(message.data().getClass())) {
        		LOGGER.debug("Invalid message type '{}' detected: {}", message.data().getClass().getName(), message.data());
        	    acknowledge(message, acknowledgment);
        	    return;
        	}
        	@SuppressWarnings("unchecked")
        	final ConsumerRecord<String, T> kafkaRecord = (ConsumerRecord<String, T>) message.internalMessage();
        	LOGGER.debug("Handling message from kafka topic {} partition {} offset {}: {}", kafkaRecord.topic(), kafkaRecord.partition(), kafkaRecord.offset(), message);
        	
            // Save message
			messagePersistence.read(kafkaRecord, acknowledgment, consumption, category);
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

	@Override
	public Class<T> messageType() {
		return messageType;
	}

	@Override
	public String topic() {
		return topic;
	}

	/**
	 * Acknowledge KAFKA message
	 *
	 */
	final void acknowledge(final Message<T> message,
						   final Acknowledgement acknowledgment) {
		try {
			LOGGER.debug("Acknowledging KAFKA message: {}", message.data());
			acknowledgment.acknowledge();
		} catch (final Exception e) {
			@SuppressWarnings("unchecked")
			ConsumerRecord<String, T> kafkaRecord = (ConsumerRecord<String, T>) message.internalMessage();

			LOGGER.error(
					"Error on acknowledging KAFKA message (topic: {}, partition: {}, offset: {}) {} : {}",
					kafkaRecord.topic(),
					kafkaRecord.partition(),
					kafkaRecord.offset(),
					kafkaRecord.value(),
					LogUtils.toString(e)
			);
		}
	}
}
