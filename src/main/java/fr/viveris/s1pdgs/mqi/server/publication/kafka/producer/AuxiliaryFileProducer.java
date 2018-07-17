package fr.viveris.s1pdgs.mqi.server.publication.kafka.producer;

import fr.viveris.s1pdgs.mqi.model.queue.AuxiliaryFileDto;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;

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
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final AuxiliaryFileDto obj) {
        return obj.getProductName();
    }

}
