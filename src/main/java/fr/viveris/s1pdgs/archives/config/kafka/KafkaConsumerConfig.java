/**
 * 
 */
package fr.viveris.s1pdgs.archives.config.kafka;

import java.net.InetAddress;
import java.net.UnknownHostException;
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

import fr.viveris.s1pdgs.archives.controller.dto.SliceDto;
import fr.viveris.s1pdgs.archives.controller.dto.ReportDto;


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
	 * Client identifier for KAFKA
	 */
	@Value("${kafka.client-id}")
	protected String kafkaClientId;
	/**
	 * Pool timeout for consumption
	 */
	@Value("${kafka.poll-timeout}")
	private long kafkaPooltimeout;
	
	@Value("${kafka.max-pool-records}")
	protected int kafkaMaxPoolRecords;

	@Value("${kafka.session-timeout-ms}")
	protected int kafkaSessionTimeoutMs;

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
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaMaxPoolRecords);
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaSessionTimeoutMs);
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

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// SLICES
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, SliceDto> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(SliceDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, SliceDto>> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, SliceDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}

	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------
	// REPORTS
	// ------------------------------------------------------------------------
	// ------------------------------------------------------------------------

	/**
	 * Consumer factory
	 * @return
	 */
	@Bean(name = "reportConsumerFactory")
	public ConsumerFactory<String, ReportDto> reportConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), new JsonDeserializer<>(ReportDto.class));
	}

	/**
	 * Listener containers factory
	 * @return
	 */
	@Bean(name = "reportKafkaListenerContainerFactory")
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, ReportDto>> reportKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ReportDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(reportConsumerFactory());
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		return factory;
	}
	
}
