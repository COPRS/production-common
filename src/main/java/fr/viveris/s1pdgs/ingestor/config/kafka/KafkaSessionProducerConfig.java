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

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.ingestor.services.kafka.KafkaSessionProducer;

/**
 * Kafka producer dedicated to the topic "sessions"
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableKafka
public class KafkaSessionProducerConfig {
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
	public Map<String, Object> producerSessionConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return props;
	}

	/**
	 * Producer factory
	 * @return
	 */
	@Bean
	public ProducerFactory<String, KafkaEdrsSessionDto> producerSessionFactory() {
		return new DefaultKafkaProducerFactory<>(producerSessionConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * @return
	 */
	@Bean
	public KafkaTemplate<String, KafkaEdrsSessionDto> kafkaSessionTemplate() {
		return new KafkaTemplate<>(producerSessionFactory());
	}

	/**
	 * KAFKA producer
	 * @return
	 */
	@Bean
	public KafkaSessionProducer senderSession() {
		return new KafkaSessionProducer();
	}
}
