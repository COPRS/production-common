package esa.s1pdgs.cpoc.mqi.server.consumption.kafka.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.client.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
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
public class GenericMessageListener<T>
        implements AcknowledgingConsumerAwareMessageListener<String, T> {

    /**
     * Logger
     */
    private static final Logger LOGGER =
            LogManager.getLogger(GenericMessageListener.class);

    /**
     * Properties
     */
    private final KafkaProperties properties;

    /**
     * Service for persisting data
     */
    private final GenericAppCatalogMqiService<T> service;

    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;

    /**
     * Generic consumer
     */
    private final GenericConsumer<T> genericConsumer;

    /**
     * Application status
     */
    private final AppStatus appStatus;

    /**
     * Constructor
     * 
     * @param properties
     * @param service
     * @param otherAppService
     * @param genericConsumer
     */
    public GenericMessageListener(final KafkaProperties properties,
            final GenericAppCatalogMqiService<T> service,
            final OtherApplicationService otherAppService,
            final GenericConsumer<T> genericConsumer,
            final AppStatus appStatus) {
        this.properties = properties;
        this.service = service;
        this.otherAppService = otherAppService;
        this.genericConsumer = genericConsumer;
        this.appStatus = appStatus;
    }

    /**
     * Listener. Method call when a message is received
     */
    @Override
    public void onMessage(final ConsumerRecord<String, T> data,
            final Acknowledgment acknowledgment,
            final Consumer<?, ?> consumer) {

        try {
            // Save message
            MqiLightMessageDto result = service.read(data.topic(),
                    data.partition(), data.offset(),
                    new MqiGenericReadMessageDto<T>(
                            properties.getConsumer().getGroupId(),
                            properties.getHostname(), false, data.value()));

            // Deal with result
            switch (result.getState()) {
                case ACK_KO:
                case ACK_OK:
                case ACK_WARN:
                    // We ignore the message
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("We ignore message {} and go the next",
                                data);
                    }
                    acknowlegde(data, acknowledgment);
                    break;
                case SEND:
                    // Message already processing
                    if (properties.getHostname()
                            .equals(result.getSendingPod())) {
                        // Message processing by myself
                        acknowlegde(data, acknowledgment);
                        pause();
                    } else {
                        // Message processing by another pod
                        if (!messageShallBeIgnored(data, result)) {
                            // We have forced the reading
                            acknowlegde(data, acknowledgment);
                            pause();
                        } else {
                            // We ignore the message
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug(
                                        "We ignore message {} and go the next",
                                        data);
                            }
                            acknowlegde(data, acknowledgment);
                        }
                    }
                    break;
                default:
                    // Message assigned
                    acknowlegde(data, acknowledgment);
                    pause();
                    break;
            }
            appStatus.resetError();
        } catch (AbstractCodedException e) {
            LOGGER.error(
                    "{} we cannot acknowledged this message and try the next time; Set app status in error",
                    e.getLogMessage());
            appStatus.setError();
        }
    }

    /**
     * Pause the consumer
     */
    protected void pause() {
        this.genericConsumer.pause();
    }

    /**
     * Acknowledge KAFKA message
     * 
     * @param data
     * @param acknowledgment
     */
    protected void acknowlegde(final ConsumerRecord<String, T> data,
            final Acknowledgment acknowledgment) {
        try {
            acknowledgment.acknowledge();
        } catch (Exception exc) {
            LOGGER.error(
                    "[topic {}] [partition {}] [offset {}] Cannot ack KAFKA message: {}",
                    data.topic(), data.partition(), data.offset(),
                    exc.getMessage());
        }
    }

    /**
     * true if the message shall be ignored, false else
     * 
     * @param lightMessage
     * @return
     * @throws AbstractCodedException
     */
    protected boolean messageShallBeIgnored(
            final ConsumerRecord<String, T> data,
            final MqiLightMessageDto lightMessage)
            throws AbstractCodedException {
        boolean ret = false;
        // Ask to the other application
        try {
            ret = otherAppService.isProcessing(lightMessage.getSendingPod(),
                    service.getCategory(), lightMessage.getIdentifier());
        } catch (AbstractCodedException ace) {
            ret = false;
            LOGGER.warn(
                    "{} No response from the other application, consider it as dead",
                    ace.getLogMessage());
        }
        if (!ret) {
            MqiLightMessageDto resultForce = service.read(data.topic(),
                    data.partition(), data.offset(),
                    new MqiGenericReadMessageDto<T>(
                            properties.getConsumer().getGroupId(),
                            properties.getHostname(), true, data.value()));
            if (resultForce.getState() != MqiStateMessageEnum.READ) {
                ret = true;
            }
            LOGGER.warn(
                    "We force the reading for the message {}, will the message be ignored {}",
                    lightMessage, ret);
        }
        return ret;
    }

}
