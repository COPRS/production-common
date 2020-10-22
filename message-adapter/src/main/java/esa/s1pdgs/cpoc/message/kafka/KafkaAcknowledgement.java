package esa.s1pdgs.cpoc.message.kafka;

import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.message.Acknowledgement;

public class KafkaAcknowledgement implements Acknowledgement {

    private final Acknowledgment acknowledgment;

    public KafkaAcknowledgement(Acknowledgment acknowledgment) {
        this.acknowledgment = acknowledgment;
    }

    @Override
    public void acknowledge() {
        acknowledgment.acknowledge();
    }
}
