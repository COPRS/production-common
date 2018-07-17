package fr.viveris.s1pdgs.mqi.server.publication.kafka.producer;

import fr.viveris.s1pdgs.mqi.model.LevelReportDto;
import fr.viveris.s1pdgs.mqi.server.KafkaProperties;

/**
 * Implementation of AbstractGenericProducer for the category LEVEL_REPORTS
 * 
 * @author Viveris Technologies
 */
public class LevelReportProducer
        extends AbstractGenericProducer<LevelReportDto> {

    /**
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    public LevelReportProducer(final KafkaProperties properties) {
        super(properties);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final LevelReportDto obj) {
        return obj.getProductName();
    }

}
