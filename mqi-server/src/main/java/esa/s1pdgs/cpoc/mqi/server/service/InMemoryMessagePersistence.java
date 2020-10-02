package esa.s1pdgs.cpoc.mqi.server.service;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.MessageState;
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
    private final Map<String, Map<Integer, Long>> acknowledgedOffsets;

    public InMemoryMessagePersistence(final KafkaProperties properties, final PersistenceConfiguration.InMemoryMessagePersistenceConfiguration configuration) {
        this.properties = properties;
        this.defaultOffset = configuration.getDefaultOffset();
        this.messageThresholdHigh = configuration.getInMemoryPersistenceHighThreshold();
        acknowledgedOffsets = new ConcurrentHashMap<>();
    }

    @Override
    public void read(final ConsumerRecord<String, T> data, final Acknowledgment acknowledgment, final GenericConsumer<T> genericConsumer, final ProductCategory category) {

        if(messageAlreadyRead(data)) {
            LOG.info("Message from kafka topic {} partition {} offset {}: already read, skipping",
                    data.topic(), data.partition(), data.offset());
            return;
        }

        if(messageBelowMaxOffset(data)) {
            LOG.info("Message from kafka topic {} partition {} offset {}: already acknowledged, skipping",
                    data.topic(), data.partition(), data.offset());
            return;
        }

        final AppCatMessageDto<T> newEntry = new AppCatMessageDto<>(category, sequence.incrementAndGet(), data.topic(), data.partition(), data.offset());
        newEntry.setCreationDate(new Date());
        newEntry.setDto(data.value());
        newEntry.setOffset(data.offset());
        newEntry.setPartition(data.partition());
        newEntry.setGroup(properties.getConsumer().getGroupId());
        newEntry.setTopic(data.topic());
        newEntry.setReadingPod(properties.getHostname()); //readingPod = body.getPod (see esa.s1pdgs.cpoc.appcatalog.server.service.MessageManager.insertOrUpdate)
        newEntry.setLastReadDate(new Date());
        newEntry.setState(MessageState.READ);
        //TODO any else fields to set?
        messages.add(new MessageAndAcknowledgement<>(newEntry, acknowledgment));

        validateMessageSizesForConsumer(genericConsumer);
    }

    private boolean messageBelowMaxOffset(ConsumerRecord<String,T> data) {
        String topic = data.topic();
        Integer partition = data.partition();
        long offset = data.offset();

        if(!acknowledgedOffsets.containsKey(topic)) {
            return false;
        }

        if(!acknowledgedOffsets.get(topic).containsKey(partition)) {
            return false;
        }

        return offset <= acknowledgedOffsets.get(topic).get(partition);
    }

    private boolean messageAlreadyRead(ConsumerRecord<String, T> data) {
        return messages.stream().anyMatch(
                m -> m.message.getTopic().equals(data.topic())
                        && m.message.getPartition() == data.partition()
                        && m.message.getOffset() == data.offset()
        );
    }

    @Override
    public List<AppCatMessageDto<T>> next(ProductCategory category, String podName) {
        return messages.stream()
                .filter(m -> m.message.getCategory().equals(category) && m.message.getReadingPod().equals(podName))
                .map(m -> m.message)
                .collect(toList());
    }

    @Override
    public boolean send(ProductCategory category, long messageId, AppCatSendMessageDto body) {
        final AppCatMessageDto<T> messageDto = get(category, messageId);

        messageDto.setLastReadDate(new Date());
        messageDto.setState(MessageState.SEND);

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
        AppCatMessageDto<T> message = messageDto.get().message;
        LOG.debug("Message from kafka topic {} partition {} offset {}: acknowledged",
                message.getTopic(), message.getPartition(), message.getOffset());

        updateAcknowledgedOffsetFor(message.getTopic(), message.getPartition(), message.getOffset());

        return true;
    }

    private void updateAcknowledgedOffsetFor(String topic, int partition, long offset) {
        if(!acknowledgedOffsets.containsKey(topic)) {
            acknowledgedOffsets.put(topic, new ConcurrentHashMap<>());
        }

        acknowledgedOffsets.get(topic).put(partition, offset);
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

        //always return default offset (we don't want to re consume messages again)
        return defaultOffset;

    }

    @Override
    public void handlePartitionRevoke(String topic, int partition) {
        List<MessageAndAcknowledgement<T>> toBeRemoved =
                messages.stream().filter(
                        m -> m.message.getTopic().equals(topic) && m.message.getPartition() == partition
                                && m.message.getState() == MessageState.READ
                ).collect(toList());
        messages.removeAll(toBeRemoved);
        String maxOffset =
                toBeRemoved.stream()
                        .max(comparingLong(a -> a.message.getOffset()))
                        .map(m -> String.valueOf(m.message.getOffset())).orElse("n/a");
        LOG.debug("removed {} pre-fetched messages up to offset {} for revoked topic {} partition {}", toBeRemoved.size(), maxOffset, topic, partition);
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
