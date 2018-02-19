package fr.viveris.s1pdgs.ingestor.config.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.ingestor.services.kafka.KafkaMetadataProducer;

/**
 * Kafka producer dedicated to the topic "metadata"
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableKafka
public class KafkaMetadataProducerConfig {

	/**
	 * URI of KAFKA cluster
	 */
	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;

	/**
	 * Producer configuration
	 * @return
	 */
	@Bean
	public Map<String, Object> producerMetadataConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);
		return props;
	}

	/**
	 * Producer factory
	 * @return
	 */
	@Bean
	public ProducerFactory<String, KafkaConfigFileDto> producerMetadataFactory() {
		return new DefaultKafkaProducerFactory<>(producerMetadataConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * @return
	 */
	@Bean
	public KafkaTemplate<String, KafkaConfigFileDto> kafkaMetadataTemplate() {
		return new KafkaTemplate<>(producerMetadataFactory());
	}

	/**
	 * KAFKA producer
	 * @return
	 */
	@Bean
	public KafkaMetadataProducer senderMetadata() {
		return new KafkaMetadataProducer();
	}
}
