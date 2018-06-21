package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0AcnDto;

/**
 * @author Viveris Technologies
 */
@Service
public class L0ACNsProducer extends AbstractGenericProducer<L0AcnDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param topic
     */
    @Autowired
    public L0ACNsProducer(
            @Qualifier("kafkaAcnTemplate") final KafkaTemplate<String, L0AcnDto> kafkaTemplate,
            @Value("${kafka.topic.l0-acns}") final String topic) {
        super(kafkaTemplate, topic);
    }

    /**
     * @see AbstractGenericProducer#extractProductName(Object)
     */
    @Override
    protected String extractProductName(final L0AcnDto obj) {
        return obj.getProductName();
    }

}
