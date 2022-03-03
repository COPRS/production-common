package esa.s1pdgs.cpoc.message.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.message.Acknowledgement;

public class KafkaAcknowledgement<M> implements Acknowledgement {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaAcknowledgement.class);

    private final Acknowledgment acknowledgment;
    private final ConsumerRecord<String, M> consumerRecord;

    public KafkaAcknowledgement(Acknowledgment acknowledgment, ConsumerRecord<String, M> consumerRecord) {
        this.acknowledgment = acknowledgment;
        this.consumerRecord = consumerRecord;
    }

    @Override
    public void acknowledge() {
        LOG.info("acknowledge message in topic {} partition {} offset {}",
                consumerRecord.topic(),
                consumerRecord.partition(),
                consumerRecord.offset());
        acknowledgment.acknowledge();
    }
}
