/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.config.kafka;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.mdcatalog.services.kafka.KafkaEdrsSessionFileConsumer;

/**
 * KAFKA consumer configuration
 * @author Olivier Bex-Chauvet
 *
 */
@Configuration
@EnableKafka
public class KafkaEdrsSessionFileConsumerConfig {
	
	/**
	 * URI of KAFKA cluster
	 */
	@Value("${kafka.bootstrap-servers}")
	private String bootstrapServers;
	/**
	 * Group identifier for KAFKA
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
	public ConsumerFactory<String, KafkaEdrsSessionDto> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaEdrsSessionDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean(name = "sessionKafkaListenerContainerFactory")
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaEdrsSessionDto>> kafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaEdrsSessionDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	/**
	 * KAFKA consumer for Config File
	 * @return
	 */
	@Bean
	public KafkaEdrsSessionFileConsumer receiver() {
		return new KafkaEdrsSessionFileConsumer();
	}
}
