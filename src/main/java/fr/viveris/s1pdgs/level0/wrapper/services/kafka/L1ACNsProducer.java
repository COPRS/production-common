package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1AcnDto;

/**
 * @author Viveris Technologies
 */
@Service
public class L1ACNsProducer extends AbstractGenericProducer<L1AcnDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param topic
     */
    @Autowired
    public L1ACNsProducer(
            @Qualifier("kafkaL1AcnTemplate") final KafkaTemplate<String, L1AcnDto> kafkaTemplate,
            @Value("${kafka.topic.l1-acns}") final String topic) {
        super(kafkaTemplate, topic);
    }

    /**
     * @see AbstractGenericProducer#extractProductName(Object)
     */
    @Override
    protected String extractProductName(final L1AcnDto obj) {
        return obj.getProductName();
    }

}