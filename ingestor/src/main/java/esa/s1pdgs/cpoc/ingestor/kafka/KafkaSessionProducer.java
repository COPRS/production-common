package esa.s1pdgs.cpoc.ingestor.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;

/**
 * KAFKA producer for publishing session information. </br>
 * Produce a message in topic of session.
 * 
 * @author Cyrielle Gailliard
 */
@Service
public class KafkaSessionProducer extends AbstractKafkaService<EdrsSessionDto> {

    /**
     * Constructor
     * 
     * @param kafkaTemplate
     * @param kafkaTopic
     */
    @Autowired
    public KafkaSessionProducer(
            @Qualifier("kafkaSessionTemplate") final KafkaTemplate<String, EdrsSessionDto> kafkaTemplate,
            @Value("${kafka.topic.edrs-sessions}") final String kafkaTopic) {
        super(kafkaTemplate, kafkaTopic);
    }

    /**
     * 
     */
    @Override
    protected String extractProductName(EdrsSessionDto obj) {
        return obj.getKeyObjectStorage();
    }

}