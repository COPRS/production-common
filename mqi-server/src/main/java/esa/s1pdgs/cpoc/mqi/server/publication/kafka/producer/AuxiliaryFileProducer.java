package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category AUXILIARY_FILES
 * 
 * @author Viveris Technologies
 */
public class AuxiliaryFileProducer
        extends AbstractGenericProducer<AuxiliaryFileDto> {

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public AuxiliaryFileProducer(final KafkaProperties properties) {
        super(properties);
    }

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    protected AuxiliaryFileProducer(final KafkaProperties properties,
            final KafkaTemplate<String, AuxiliaryFileDto> template) {
        super(properties, template);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final AuxiliaryFileDto obj) {
        return obj.getProductName();
    }

}
