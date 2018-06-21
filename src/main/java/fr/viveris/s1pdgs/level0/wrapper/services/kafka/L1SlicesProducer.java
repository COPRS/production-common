package fr.viveris.s1pdgs.level0.wrapper.services.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1SliceDto;

/**
 * @author Viveris Technologies
 */
@Service
public class L1SlicesProducer extends AbstractGenericProducer<L1SliceDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param topic
     */
    @Autowired
    public L1SlicesProducer(final KafkaTemplate<String, L1SliceDto> kafkaTemplate,
            @Value("${kafka.topic.l1-slices}") final String topic) {
        super(kafkaTemplate, topic);
    }

    /**
     * @see AbstractGenericProducer#extractProductName(Object)
     */
    @Override
    protected String extractProductName(final L1SliceDto obj) {
        return obj.getProductName();
    }

}