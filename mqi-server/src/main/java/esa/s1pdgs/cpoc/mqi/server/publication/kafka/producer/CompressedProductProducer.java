package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import esa.s1pdgs.cpoc.mqi.model.queue.ProductDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

public class CompressedProductProducer extends AbstractGenericProducer<ProductDto> {
    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public CompressedProductProducer(final KafkaProperties properties) {
        super(properties);
    }
    
    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final ProductDto obj) {
        return obj.getProductName();
    }
}
