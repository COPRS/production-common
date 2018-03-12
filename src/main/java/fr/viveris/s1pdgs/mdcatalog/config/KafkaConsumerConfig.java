/**
 * 
 */
package fr.viveris.s1pdgs.mdcatalog.config;

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

import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaEdrsSessionDto;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL0AcnDto;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL0SliceDto;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL1ADto;
import fr.viveris.s1pdgs.mdcatalog.model.dto.KafkaL1SliceDto;

/**
 * KAFKA consumer configuration
 * @author Olivier Bex-Chauvet
 *
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

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

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// CONFIG FILES
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

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
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// EDRS SESSION FILES
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, KafkaEdrsSessionDto> edrsSessionsConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaEdrsSessionDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaEdrsSessionDto>> edrsSessionsKafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaEdrsSessionDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(edrsSessionsConsumerFactory());
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// L0 SLICES
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, KafkaL0SliceDto> l0SlicesConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaL0SliceDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaL0SliceDto>> l0SlicesKafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaL0SliceDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(l0SlicesConsumerFactory());
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// L0 ACNS
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, KafkaL0AcnDto> l0AcnsConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaL0AcnDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaL0AcnDto>> l0AcnsKafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaL0AcnDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(l0AcnsConsumerFactory());
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}
	
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// L1 SLICES
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, KafkaL1SliceDto> l1SlicesConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaL1SliceDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaL1SliceDto>> l1SlicesKafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaL1SliceDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(l1SlicesConsumerFactory());
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// L0 ACNS
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, KafkaL1ADto> l1AConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(KafkaL1ADto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, KafkaL1ADto>> l1AKafkaListenerContainerFactory() {
		
		ConcurrentKafkaListenerContainerFactory<String, KafkaL1ADto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(l1AConsumerFactory());
		factory.setConcurrency(1);
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}
	
}
