package esa.s1pdgs.cpoc.mqi.server.publication.kafka.producer;

import org.springframework.kafka.core.KafkaTemplate;

import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;

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
     * Constructor
     * 
     * @param properties
     * @param topic
     */
    protected LevelReportProducer(final KafkaProperties properties,
            final KafkaTemplate<String, LevelReportDto> template) {
        super(properties, template);
    }

    /**
     * Extract the product name from the DTO objects
     */
    @Override
    protected String extractProductName(final LevelReportDto obj) {
        return obj.getProductName();
    }

}
