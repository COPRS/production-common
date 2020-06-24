package esa.s1pdgs.cpoc.mqi.server.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatReadMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;

public class InMemoryMessagePersistence<T extends AbstractMessage> implements MessagePersistence<T> {

    private final AtomicLong sequence = new AtomicLong(0);
    private final Queue<MessageAndAcknowledgement<T>> messages = new ConcurrentLinkedDeque<>();
    private final KafkaProperties properties;
    private final ProductCategory category;

    public InMemoryMessagePersistence(final KafkaProperties properties, final ProductCategory category) {
        this.properties = properties;
        this.category = category;
    }

    @Override
    public void read(ConsumerRecord<String, T> data, Acknowledgment acknowledgment) {
        AppCatReadMessageDto<T> body = new AppCatReadMessageDto<>(
                properties.getConsumer().getGroupId(),
                properties.getHostname(),
                false,
                data.value()
        );

        final AppCatMessageDto<T> newEntry = new AppCatMessageDto<>(category, sequence.incrementAndGet(), data.topic(), data.partition(), data.offset());
        newEntry.setCreationDate(new Date());
        newEntry.setDto(body.getDto());
        newEntry.setGroup(body.getGroup());
        newEntry.setReadingPod(body.getPod()); //readingPod = body.getPod (see esa.s1pdgs.cpoc.appcatalog.server.service.MessageManager.insertOrUpdate)
        //TODO any else fields to set?
        messages.add(new MessageAndAcknowledgement<>(newEntry, acknowledgment));

        //TODO add check for size of queue in order to avoid memory leak (pause kafka container on certain size)
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
        get(category, messageId); //just check for existence;

        return true; //always true, we don't check double messages here
    }

    @Override
    public boolean ack(ProductCategory category, long messageId, Ack ack) {

        final Optional<MessageAndAcknowledgement<T>> messageDto = getInternal(messageId);

        if(!messageDto.isPresent()) {
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
        final Optional<MessageAndAcknowledgement<T>> message =getInternal(messageId);

        if (!message.isPresent()) {
            throw new IllegalArgumentException("message with id " + messageId + " not found");
        }

        return message.get();
    }

    @Override
    public int getNbReadingMessages(String topic, String podName) {
        return (int) messages.stream().filter(m -> m.message.getTopic().equals(topic) && m.message.getReadingPod().equals(podName)).count();
    }

    @Override
    public long getEarliestOffset(String topic, int partition, String group) {
        return 0;
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
