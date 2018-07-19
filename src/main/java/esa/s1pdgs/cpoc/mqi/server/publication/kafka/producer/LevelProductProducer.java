package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category LEVEL_PRODUCTS
 * 
 * @author Viveris Technologies
 */
public class LevelProductProducer
        extends AbstractGenericProducer<LevelProductDto> {

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public LevelProductProducer(final KafkaProperties properties) {
        super(properties);
    }

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    protected LevelProductProducer(final KafkaProperties properties,
            final KafkaTemplate<String, LevelProductDto> template) {
        super(properties, template);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final LevelProductDto obj) {
        return obj.getProductName();
    }

}
