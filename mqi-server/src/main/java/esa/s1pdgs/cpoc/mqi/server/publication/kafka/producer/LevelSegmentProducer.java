package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category LEVEL_PRODUCTS
 * 
 * @author Viveris Technologies
 */
public class LevelSegmentProducer
        extends AbstractGenericProducer<LevelSegmentDto> {

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public LevelSegmentProducer(final KafkaProperties properties) {
        super(properties);
    }

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    protected LevelSegmentProducer(final KafkaProperties properties,
            final KafkaTemplate<String, LevelSegmentDto> template) {
        super(properties, template);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final LevelSegmentDto obj) {
        return obj.getName();
    }

}
