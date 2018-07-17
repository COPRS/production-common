package fr.viveris.s1pdgs.mqi.server.publication.kafka.producer;

import fr.viveris.s1pdgs.mqi.model.LevelJobDto;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category LEVEL_JOBS
 * 
 * @author Viveris Technologies
 */
public class LevelJobProducer extends AbstractGenericProducer<LevelJobDto> {

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public LevelJobProducer(final KafkaProperties properties) {
        super(properties);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final LevelJobDto obj) {
        return obj.getProductIdentifier();
    }

}
