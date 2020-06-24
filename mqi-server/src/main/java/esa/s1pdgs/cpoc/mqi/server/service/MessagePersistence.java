package esa.s1pdgs.cpoc.mqi.server.service;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.model.queue.AbstractMessage;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;

public interface MessagePersistence<T extends  AbstractMessage> {

    void read (final ConsumerRecord<String, T> data, final Acknowledgment acknowledgment) throws Exception;

    List<AppCatMessageDto<T>> next(final ProductCategory category, String podName) throws AbstractCodedException;

    boolean send(final ProductCategory category, final long messageId, final AppCatSendMessageDto body) throws AbstractCodedException;

    boolean ack(final ProductCategory category, final long messageId, final Ack ack) throws AbstractCodedException;

    AppCatMessageDto<T> get(final ProductCategory category, final long messageId) throws AbstractCodedException;

    int getNbReadingMessages(final String topic, final String podName) throws AbstractCodedException;

    long getEarliestOffset(String topic, int partition, String group) throws AbstractCodedException;
}
