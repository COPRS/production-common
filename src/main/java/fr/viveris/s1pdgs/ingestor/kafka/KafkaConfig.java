package fr.viveris.s1pdgs.ingestor.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import fr.viveris.s1pdgs.ingestor.kafka.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.ingestor.kafka.dto.KafkaEdrsSessionDto;

@Configuration
@EnableKafka
public class KafkaConfig {
	
	/**
	 * URI of KAFKA cluster
	 */
	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;
	/**
	 * Ingestor group identifier for KAFKA
	 */
	@Value("${kafka.group-id}")
	private String kafkaGroupId;
	/**
	 * Pool timeout for consumption
	 */
	@Value("${kafka.poll-timeout}")
	private long kafkaPooltimeout;

	/**
	 * Consumer configuration
	 * @return
	 */
	@Bean
	public Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
		return props;
	}

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, KafkaConfigFileDto> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaConfigFileDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaConfigFileDto>> kafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaConfigFileDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.getContainerProperties();
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	/**
	 * Producer configuration
	 * @return
	 */
	@Bean
	public Map<String, Object> producerConfig() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		return props;
	}

	/**
	 * Producer factory
	 * @return
	 */
	@Bean(name="producerSessionFactory")
	public ProducerFactory<String, KafkaEdrsSessionDto> producerSessionFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfig());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * @return
	 */
	@Bean(name="kafkaSessionTemplate")
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

	/**
	 * Producer factory
	 * @return
	 */
	@Bean(name="producerConfigFileFactory")
	public ProducerFactory<String, KafkaConfigFileDto> producerConfigFileFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfig());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * @return
	 */
	@Bean(name="kafkaConfigFileTemplate")
	public KafkaTemplate<String, KafkaConfigFileDto> kafkaConfigFileTemplate() {
		return new KafkaTemplate<>(producerConfigFileFactory());
	}

	/**
	 * KAFKA producer
	 * @return
	 */
	@Bean
	public KafkaConfigFileProducer senderConfigFiles() {
		return new KafkaConfigFileProducer();
	}

}
