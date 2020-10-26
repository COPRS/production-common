package esa.s1pdgs.cpoc.mqi.server.service;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.AppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.message.Acknowledgement;
import esa.s1pdgs.cpoc.message.Consumption;
import esa.s1pdgs.cpoc.message.kafka.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

public class AppCatalogMessagePersistence<T extends AbstractMessage> implements MessagePersistence<T> {

    private static final Logger LOGGER = LogManager.getLogger(AppCatalogMessagePersistence.class);

    private final AppCatalogMqiService<T> appCatalogMqiService;
    private final KafkaProperties properties;
    private final OtherApplicationService otherAppService;

    public AppCatalogMessagePersistence(final AppCatalogMqiService<T> appCatalogMqiService,
                                        final KafkaProperties properties,
                                        final OtherApplicationService otherAppService) {

        this.appCatalogMqiService = appCatalogMqiService;
        this.properties = properties;
        this.otherAppService = otherAppService;
    }

    @Override
    public void read(final ConsumerRecord<String, T> data, final Acknowledgement acknowledgment, final Consumption consumption, final ProductCategory category) throws Exception {
        final AppCatMessageDto<T> result = saveInAppCat(data, false, category);
        handleMessage(data, acknowledgment, result, consumption, category);
    }

    @Override
    public List<AppCatMessageDto<T>> next(final ProductCategory category, final String podName) throws AbstractCodedException {
        return appCatalogMqiService.next(category, podName);
    }

    @Override
    public boolean send(final ProductCategory category, final long messageId, final AppCatSendMessageDto body) throws AbstractCodedException {
        return appCatalogMqiService.send(category, messageId, body);
    }

    @Override
    public boolean ack(final ProductCategory category, final long messageId, final Ack ack) throws AbstractCodedException {
        return appCatalogMqiService.ack(category, messageId, ack);
    }

    @Override
    public AppCatMessageDto<T> get(final ProductCategory category, final long messageId) throws AbstractCodedException {
        return appCatalogMqiService.get(category, messageId);
    }

    @Override
    public int getNbReadingMessages(final String topic, final String podName) throws AbstractCodedException {
        return appCatalogMqiService.getNbReadingMessages(topic, podName);
    }

    @Override
    public long getEarliestOffset(final String topic, final int partition, final String group) throws AbstractCodedException {
        return appCatalogMqiService.getEarliestOffset(topic, partition, group);
    }

    /* 
     * Ok, from my understanding of MQI, all this weird handling should result in following behavior:
     * For all messages that are not in state ACK (which is ignored), the consumer is paused on successful consumption 
     * of a single message (which has been persisted in mongo). So as long as the message is not handled by the service,
     * no new message will be consumed by this MQI instance, so sibling instances on other pods may consume them from
     * kafka.
     * This should work and be safe for almost all scenarios. Though, if MQI has an active message in mongo and the pod 
     * dies (e.g. by downscaling), the message will remain in mongo forever and no other pod will ever deal with it, if 
     * there is no other logic dealing with such scenarios (maybe in app-cat).
     */
    final void handleMessage(
            final ConsumerRecord<String, T> data,
            final Acknowledgement acknowledgment,
            final AppCatMessageDto<T> result,
            final Consumption consumption,
            final ProductCategory category) throws AbstractCodedException {
        final T message = data.value();

        // Deal with result
        switch (result.getState()) {
            case ACK_KO:
            case ACK_OK:
            case ACK_WARN:
                LOGGER.debug("Message ignored and going to the next (state is {}): {}", result.getState(), message);
                acknowledge(data, acknowledgment);
                break;
            case SEND:
                LOGGER.debug("Message {} is already processing (state is SEND)", result.getId());
                if (properties.getHostname().equals(result.getSendingPod())) {
                    LOGGER.debug("Message {} already processed by this pod (state is SEND). Ignoring and pausing consumption",
                            result.getId());
                    acknowledge(data, acknowledgment);
                    consumption.pause();
                } else {
                    // Message processing by another pod
                    if (messageShallBeIgnored(data, result, category)) {
                        LOGGER.debug("Message {} shall be ignored (state is SEND). Ignoring...", result.getId());
                        acknowledge(data, acknowledgment);
                    } else {
                        LOGGER.debug("Forced message {} transition from SEND to READ). Pausing consumption",
                                result.getId());
                        // We have forced the reading
                        acknowledge(data, acknowledgment);
                        consumption.pause();
                    }
                }
                break;
            default:
                // Message assigned
                LOGGER.debug("Message {} assigned to this pod. Pausing consumption ...", result.getId());
                acknowledge(data, acknowledgment);
                consumption.pause();
                break;
        }
    }

    /**
     * @return true if the message shall be ignored, false else
     */
    final boolean messageShallBeIgnored(
            final ConsumerRecord<String, T> data,
            final AppCatMessageDto<T> mess, final ProductCategory category)
            throws AbstractCodedException {
        boolean ret;
        // Ask to the other application
        try {
            ret = otherAppService.isProcessing(mess.getSendingPod(), category, mess.getId());
        } catch (final AbstractCodedException ace) {
            ret = false;
            LOGGER.warn("{} No response from the other application, consider it as dead", ace.getLogMessage());
        }
        if (!ret) {
            LOGGER.debug("No other pod is handling the message {}. Enforcing update to state READ...", mess.getId());
            final AppCatMessageDto<T> resultForce = saveInAppCat(data, true, category);
            if (resultForce.getState() != MessageState.READ) {
                ret = true;
            }
            // no idea what this message shall mean - leave it here for historical purposes and someone understanding
            LOGGER.warn("We force the reading for the message {}, will the message be ignored {}", mess, ret);
        } else {
            LOGGER.info("Message {} is already handled by other pod", mess.getId());
        }
        return ret;
    }

    /**
     * Acknowledge KAFKA message
     */
    private void acknowledge(final ConsumerRecord<String, T> data,
                             final Acknowledgement acknowledgment) {
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

    @Override
    public void handlePartitionRevoke(String topic, int partition) {
        //nothing to do here
    }

    private AppCatMessageDto<T> saveInAppCat(final ConsumerRecord<String, T> data, final boolean force, final ProductCategory category) throws AbstractCodedException {
        return appCatalogMqiService.read(category, data.topic(), data.partition(), data.offset(), new AppCatReadMessageDto<>(
                properties.getConsumer().getGroupId(),
                properties.getHostname(),
                force,
                data.value()
        ));
    }

}
