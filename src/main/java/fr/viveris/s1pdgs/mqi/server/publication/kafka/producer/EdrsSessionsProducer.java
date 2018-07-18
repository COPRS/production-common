package fr.viveris.s1pdgs.mqi.server.publication.kafka.producer;

import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category EDRS_SESSION
 * 
 * @author Viveris Technologies
 */
public class EdrsSessionsProducer
        extends AbstractGenericProducer<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public EdrsSessionsProducer(final KafkaProperties properties) {
        super(properties);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final EdrsSessionDto obj) {
        return obj.getObjectStorageKey();
    }

}
