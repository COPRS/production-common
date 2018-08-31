package esa.s1pdgs.cpoc.ingestor.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;

/**
 * KAFKA producer for publishing metadata. </br>
 * Produce a message in topic of metadata.
 * 
 * @author Cyrielle Gailliard
 */
@Service
public class KafkaConfigFileProducer
        extends AbstractKafkaService<AuxiliaryFileDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param kafkaTopic
     */
    @Autowired
    public KafkaConfigFileProducer(
            @Qualifier("kafkaConfigFileTemplate") final KafkaTemplate<String, AuxiliaryFileDto> kafkaTemplate,
            @Value("${kafka.topic.auxiliary-files}") final String kafkaTopic) {
        super(kafkaTemplate, kafkaTopic);
    }

    /**
     * 
     */
    @Override
    protected String extractProductName(AuxiliaryFileDto obj) {
        return obj.getProductName();
    }

}
