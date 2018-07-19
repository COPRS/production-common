package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

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
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    protected LevelJobProducer(final KafkaProperties properties,
            final KafkaTemplate<String, LevelJobDto> template) {
        super(properties, template);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final LevelJobDto obj) {
        return obj.getProductIdentifier();
    }

}
