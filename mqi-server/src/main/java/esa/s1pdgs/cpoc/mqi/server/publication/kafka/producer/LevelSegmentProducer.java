package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category LEVEL_PRODUCTS
 * 
 * @author Viveris Technologies
 */
public class LevelSegmentProducer
        extends AbstractGenericProducer<ProductDto> {

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
            final KafkaTemplate<String, ProductDto> template) {
        super(properties, template);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final ProductDto obj) {
        return obj.getProductName();
    }

}
