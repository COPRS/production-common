package fr.viveris.s1pdgs.jobgenerator.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

import fr.viveris.s1pdgs.jobgenerator.controller.JobsProducer;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.EdrsSessionDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.JobDto;
import fr.viveris.s1pdgs.jobgenerator.controller.dto.L0SliceDto;

/**
 * Configuration of the KAFKA consumers and producers
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableKafka
public class KafkaConfig {

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
	 * Client identifier for KAFKA
	 */
	@Value("${kafka.client-id}")
	private String kafkaClientId;

	/**
	 * Poll timeout for consumption
	 */
	@Value("${kafka.poll-timeout}")
	private long kafkaPolltimeout;

	/**
	 * Default constructor
	 */
	public KafkaConfig() {

	}

	/**
	 * Consumer configuration:
	 * <li>ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG = this.bootstrapServers</li>
	 * <li>ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG =
	 * StringDeserializer.class</li>
	 * <li>ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG =
	 * JsonDeserializer.class</li>
	 * <li>ConsumerConfig.GROUP_ID_CONFIG = this.kafkaGroupId</li>
	 * <li>ConsumerConfig.CLIENT_ID_CONFIG = hostname or this.kafkaClientId</li>
	 * 
	 * @return
	 */
	protected Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
		try {
			InetAddress myHost = InetAddress.getLocalHost();
			String hostname = myHost.getHostName();
			props.put(ConsumerConfig.CLIENT_ID_CONFIG, hostname);
		} catch (UnknownHostException ex) {
			props.put(ConsumerConfig.CLIENT_ID_CONFIG, this.kafkaClientId);
		}
		return props;
	}

	// --------------------------------------------------------------------------------
	// EDRS SESSION CONSUMER
	// --------------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * 
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, EdrsSessionDto> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
				new JsonDeserializer<>(EdrsSessionDto.class));
	}

	/**
	 * Listener containers factory
	 * 
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, EdrsSessionDto>> kafkaListenerContainerFactory() {

		ConcurrentKafkaListenerContainerFactory<String, EdrsSessionDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.getContainerProperties();
		factory.getContainerProperties().setPollTimeout(kafkaPolltimeout);
		return factory;
	}

	// --------------------------------------------------------------------------------
	// L0 SLICES CONSUMER
	// --------------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * 
	 * @return
	 */
	@Bean(name = "l0SlicesConsumerFactory")
	public ConsumerFactory<String, L0SliceDto> l0SlicesConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
				new JsonDeserializer<>(L0SliceDto.class));
	}

	/**
	 * Listener containers factory
	 * 
	 * @return
	 */
	@Bean(name = "l0SlicesKafkaListenerContainerFactory")
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, L0SliceDto>> l0SlicesKafkaListenerContainerFactory() {

		ConcurrentKafkaListenerContainerFactory<String, L0SliceDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(l0SlicesConsumerFactory());
		factory.getContainerProperties();
		factory.getContainerProperties().setPollTimeout(kafkaPolltimeout);
		return factory;
	}

	// --------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------
	// PRODUCERS CONFIGURATION
	// --------------------------------------------------------------------------------
	// --------------------------------------------------------------------------------

	/**
	 * Producer configuration
	 * <li>ProducerConfig.BOOTSTRAP_SERVERS_CONFIG = this.bootstrapServers</li>
	 * <li>ProducerConfig.KEY_DESERIALIZER_CLASS_CONFIG =
	 * StringDeserializer.class</li>
	 * <li>ProducerConfig.VALUE_DESERIALIZER_CLASS_CONFIG =
	 * JsonDeserializer.class</li>
	 * <li>ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG = 2000</li>
	 * <li>JsonSerializer.ADD_TYPE_INFO_HEADERS = false (to be compatible with client < 1.0.0)</li>
	 * 
	 * @return
	 */
	protected Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		// TODO set in configuration
		props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 2000);
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		return props;
	}

	// --------------------------------------------------------------------------------
	// JOBS PRODUCER
	// --------------------------------------------------------------------------------

	/**
	 * Producer factory
	 * 
	 * @return
	 */
	@Bean(name = "kafkaJobsProducerFactory")
	public ProducerFactory<String, JobDto> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * 
	 * @return
	 */
	@Bean(name = "kafkaJobsTemplate")
	public KafkaTemplate<String, JobDto> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	/**
	 * KAFKA producer
	 * 
	 * @return
	 */
	@Bean(name = "kafkaJobsSender")
	public JobsProducer sender() {
		return new JobsProducer();
	}
}
