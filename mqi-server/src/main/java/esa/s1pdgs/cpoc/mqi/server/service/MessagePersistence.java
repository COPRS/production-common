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
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;

public interface MessagePersistence<T extends AbstractMessage> {

    void read(ConsumerRecord<String, T> data, Acknowledgment acknowledgment, GenericConsumer<T> genericConsumer, ProductCategory category) throws Exception;

    List<AppCatMessageDto<T>> next(ProductCategory category, String podName) throws AbstractCodedException;

    boolean send(ProductCategory category, long messageId, AppCatSendMessageDto body) throws AbstractCodedException;

    boolean ack(ProductCategory category, long messageId, Ack ack) throws AbstractCodedException;

    AppCatMessageDto<T> get(ProductCategory category, long messageId) throws AbstractCodedException;

    int getNbReadingMessages(String topic, String podName) throws AbstractCodedException;

    long getEarliestOffset(String topic, int partition, String group) throws AbstractCodedException;
}
