package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0SliceDto;

/**
 * @author Viveris Technologies
 */
@Service
public class L0SlicesProducer extends AbstractGenericProducer<L0SliceDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param topic
     */
    @Autowired
    public L0SlicesProducer(
            @Qualifier("kafkaProductTemplate") final KafkaTemplate<String, L0SliceDto> kafkaTemplate,
            @Value("${kafka.topic.l0-slices}") final String topic) {
        super(kafkaTemplate, topic);
    }

    /**
     * @see AbstractGenericProducer#extractProductName(Object)
     */
    @Override
    protected String extractProductName(final L0SliceDto obj) {
        return obj.getProductName();
    }

}
