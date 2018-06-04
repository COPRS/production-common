package fr.viveris.s1pdgs.ingestor.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import fr.viveris.s1pdgs.ingestor.files.model.dto.KafkaConfigFileDto;

/**
 * KAFKA producer for publishing metadata. </br>
 * Produce a message in topic of metadata.
 * 
 * @author Cyrielle Gailliard
 *
 */
@Service
public class KafkaConfigFileProducer extends AbstractKafkaService<KafkaConfigFileDto> {

	/**
	 * Constructor
	 * 
	 * @param kafkaTemplate
	 * @param kafkaTopic
	 */
	@Autowired
	public KafkaConfigFileProducer(
			@Qualifier("kafkaConfigFileTemplate") final KafkaTemplate<String, KafkaConfigFileDto> kafkaTemplate,
			@Value("${kafka.topic.auxiliary-files}") final String kafkaTopic) {
		super(kafkaTemplate, kafkaTopic);
	}

	/**
	 * 
	 */
	@Override
	protected String extractProductName(KafkaConfigFileDto obj) {
		return obj.getProductName();
	}

}
