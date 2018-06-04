package fr.viveris.s1pdgs.ingestor.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.files.model.dto.KafkaEdrsSessionDto;

/**
 * KAFKA producer for publishing session information. </br>
 * Produce a message in topic of session.
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaSessionProducer extends AbstractKafkaService<KafkaEdrsSessionDto> {

	/**
	 * Constructor
	 * 
	 * @param kafkaTemplate
	 * @param kafkaTopic
	 */
	@Autowired
	public KafkaSessionProducer(
			@Qualifier("kafkaSessionTemplate") final KafkaTemplate<String, KafkaEdrsSessionDto> kafkaTemplate,
			@Value("${kafka.topic.edrs-sessions}") final String kafkaTopic) {
		super(kafkaTemplate, kafkaTopic);
	}

	/**
	 * 
	 */
	@Override
	protected String extractProductName(KafkaEdrsSessionDto obj) {
		return obj.getObjectStorageKey();
	}

}