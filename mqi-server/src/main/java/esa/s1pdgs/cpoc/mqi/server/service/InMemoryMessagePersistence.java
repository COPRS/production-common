package esa.s1pdgs.cpoc.mqi.server.service;

import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.config.PersistenceConfiguration;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;

public class InMemoryMessagePersistence<T extends AbstractMessage> implements MessagePersistence<T> {

    private static final Logger LOG = LogManager.getLogger(InMemoryMessagePersistence.class);

    private final AtomicLong sequence = new AtomicLong(0);
    private final List<MessageAndAcknowledgement<T>> messages = Collections.synchronizedList(new ArrayList<>());
    private final KafkaProperties properties;
    private final int defaultOffset;
    private final int messageThresholdHigh;

    public InMemoryMessagePersistence(final KafkaProperties properties, final PersistenceConfiguration.InMemoryMessagePersistenceConfiguration configuration) {
        this.properties = properties;
        this.defaultOffset = configuration.getDefaultOffset();
        this.messageThresholdHigh = configuration.getInMemoryPersistenceHighThreshold();
    }

    @Override
    public void read(final ConsumerRecord<String, T> data, final Acknowledgment acknowledgment, final GenericConsumer<T> genericConsumer, final ProductCategory category) {

        final AppCatMessageDto<T> newEntry = new AppCatMessageDto<>(category, sequence.incrementAndGet(), data.topic(), data.partition(), data.offset());
        newEntry.setCreationDate(new Date());
        newEntry.setDto(data.value());
        newEntry.setGroup(properties.getConsumer().getGroupId());
        newEntry.setReadingPod(properties.getHostname()); //readingPod = body.getPod (see esa.s1pdgs.cpoc.appcatalog.server.service.MessageManager.insertOrUpdate)
        newEntry.setLastReadDate(new Date());
        //TODO any else fields to set?
        messages.add(new MessageAndAcknowledgement<>(newEntry, acknowledgment));

        validateMessageSizesForConsumer(genericConsumer);
    }

    @Override
    public List<AppCatMessageDto<T>> next(ProductCategory category, String podName) {
        return messages.stream()
                .filter(m -> m.message.getCategory().equals(category) && m.message.getReadingPod().equals(podName))
                .map(m -> m.message)
                .collect(Collectors.toList());
    }

    @Override
    public boolean send(ProductCategory category, long messageId, AppCatSendMessageDto body) {
        final AppCatMessageDto<T> messageDto = get(category, messageId);

        messageDto.setLastReadDate(new Date());

        return true; //always true, we don't check double messages here
    }

    @Override
    public boolean ack(ProductCategory category, long messageId, Ack ack) {

        final Optional<MessageAndAcknowledgement<T>> messageDto = getInternal(messageId);

        if (!messageDto.isPresent()) {
            return false;
        }

        messages.remove(messageDto.get());
        messageDto.get().acknowledgment.acknowledge();

        return true;
    }

    @Override
    public AppCatMessageDto<T> get(ProductCategory category, long messageId) {
        return getInternalOrThrow(messageId).message;
    }

    private Optional<MessageAndAcknowledgement<T>> getInternal(long messageId) {
        return messages.stream().filter(m -> m.message.getId() == messageId).findFirst();
    }

    private MessageAndAcknowledgement<T> getInternalOrThrow(long messageId) {
        final Optional<MessageAndAcknowledgement<T>> message = getInternal(messageId);

        if (!message.isPresent()) {
            throw new IllegalArgumentException("message with id " + messageId + " not found");
        }

        return message.get();
    }

    private void validateMessageSizesForConsumer(GenericConsumer<T> genericConsumer) {
        final long countPerTopic = messages.stream().filter(m -> m.message.getTopic().equals(genericConsumer.getTopic())).count();

        //pause consumer to avoid memory leak, resuming is already handled in MessageConsumptionController
        if (countPerTopic >= messageThresholdHigh && !genericConsumer.isPaused()) {
            LOG.info("pausing consumer for topic {} ({} messages >= threshold {})", genericConsumer.getTopic(), countPerTopic, messageThresholdHigh);
            genericConsumer.pause();
        }
    }

    @Override
    public int getNbReadingMessages(String topic, String podName) {
        return (int) messages.stream().filter(m -> m.message.getTopic().equals(topic) && m.message.getReadingPod().equals(podName)).count();
    }

    @Override
    public long getEarliestOffset(String topic, int partition, String group) {

        final Optional<MessageAndAcknowledgement<T>> earliestMessage = messages.stream()
                .filter(m -> m.message.getTopic().equals(topic) && m.message.getPartition() == partition && m.message.getGroup().equals(group))
                .min(comparing(a -> a.message.getLastReadDate()));

        if (!earliestMessage.isPresent()) {
            return defaultOffset;
        }

        return earliestMessage.get().message.getOffset();
    }

    private static class MessageAndAcknowledgement<T> {
        private final AppCatMessageDto<T> message;
        private final Acknowledgment acknowledgment;

        private MessageAndAcknowledgement(AppCatMessageDto<T> message, Acknowledgment acknowledgment) {
            this.message = message;
            this.acknowledgment = acknowledgment;
        }
    }
}
