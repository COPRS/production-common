package fr.viveris.s1pdgs.level0.wrapper.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
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
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.CollectionUtils;

import fr.viveris.s1pdgs.level0.wrapper.controller.dto.JobDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0AcnDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L0SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1AcnDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.L1SliceDto;
import fr.viveris.s1pdgs.level0.wrapper.controller.dto.ReportDto;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.L0ACNsProducer;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.L0ReportProducer;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.L0SlicesProducer;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.L1ACNsProducer;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.L1ReportProducer;
import fr.viveris.s1pdgs.level0.wrapper.services.kafka.L1SlicesProducer;

/**
 * Configuration of the KAFKA consumers and producers
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
@EnableKafka
public class KafkaConfig {

	private static final Logger LOGGER = LogManager.getLogger(KafkaConfig.class);

	/**
	 * URI of KAFKA cluster
	 */
	@Value("${kafka.bootstrap-servers}")
	protected String bootstrapServers;

	/**
	 * Ingestor group identifier for KAFKA
	 */
	@Value("${kafka.group-id}")
	protected String kafkaGroupId;

	/**
	 * Pool timeout for consumption
	 */
	@Value("${kafka.poll-timeout}")
	protected long kafkaPooltimeout;

	/**
	 * Client identifier for KAFKA
	 */
	@Value("${kafka.client-id}")
	protected String kafkaClientId;

	@Value("${kafka.max-poll-interval-ms}")
	protected int kafkaMaxPoolIntervalMs;

	@Value("${kafka.max-pool-records}")
	protected int kafkaMaxPoolRecords;

	@Value("${kafka.session-timeout-ms}")
	protected int kafkaSessionTimeoutMs;

	@Value("${kafka.heartbeat-interval-ms}")
	protected int kafkaHeartbeatIntervalMs;
	

	/**
	 * ------------------------------------------------------------------<br/>
	 * JOB CONSUMER <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Consumer configuration
	 * 
	 * @return
	 */
	protected Map<String, Object> consumerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaMaxPoolIntervalMs);
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaMaxPoolRecords);
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaSessionTimeoutMs);
		props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaHeartbeatIntervalMs);
		try {
			InetAddress myHost = InetAddress.getLocalHost();
			String hostname = myHost.getHostName();
			props.put(ConsumerConfig.CLIENT_ID_CONFIG, hostname);
		} catch (UnknownHostException ex) {
			props.put(ConsumerConfig.CLIENT_ID_CONFIG, this.kafkaClientId);
		}
		return props;
	}

	/**
	 * Consumer factory
	 * 
	 * @return
	 */
	@Bean
	public ConsumerFactory<String, JobDto> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
				new JsonDeserializer<>(JobDto.class));
	}

	/**
	 * Listener containers factory
	 * 
	 * @return
	 */
	@Bean
	public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, JobDto>> kafkaListenerContainerFactory() {

		ConcurrentKafkaListenerContainerFactory<String, JobDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		factory.getContainerProperties();
		factory.getContainerProperties().setPollTimeout(kafkaPooltimeout);
		factory.getContainerProperties().setAckMode(AbstractMessageListenerContainer.AckMode.MANUAL_IMMEDIATE);
		factory.getContainerProperties().setConsumerRebalanceListener(new ConsumerAwareRebalanceListener() {

			private boolean isPaused = false;
			
			@Override
			public void onPartitionsRevokedBeforeCommit(Consumer<?, ?> consumer,
					Collection<TopicPartition> partitions) {
				LOGGER.info("onPartitionsRevokedBeforeCommit call");
				Set<TopicPartition> pausedP = consumer.paused();
				if (!CollectionUtils.isEmpty(pausedP)) {
					isPaused = true;
					LOGGER.info("onPartitionsRevokedBeforeCommit call paused = true");
				} else {
					isPaused = false;
					LOGGER.info("onPartitionsRevokedBeforeCommit call paused = false");
				}
			}

			@Override
			public void onPartitionsRevokedAfterCommit(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
				LOGGER.info("onPartitionsRevokedAfterCommit call");
				Set<TopicPartition> pausedP = consumer.paused();
				if (!CollectionUtils.isEmpty(pausedP)) {
					isPaused = true;
					LOGGER.info("onPartitionsRevokedAfterCommit call paused = true");
				} else {
					isPaused = false;
					LOGGER.info("onPartitionsRevokedAfterCommit call paused = false");
				}
			}

			@Override
			public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {
				LOGGER.info("onPartitionsAssigned call");
				if (isPaused) {
					LOGGER.info("onPartitionsAssigned call set pause = true");
					consumer.pause(partitions);
				}
			}
		});
		return factory;
	}

	/**
	 * ------------------------------------------------------------------<br/>
	 * PRODUCER L0_PRODUCT <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Producer configuration for L0 Slices
	 * 
	 * @return
	 */
	public Map<String, Object> producerConfigs() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
		return props;
	}

	/**
	 * Producer factory for L0Slices
	 * 
	 * @return
	 */
	@Bean(name = "producerProductFactory")
	public ProducerFactory<String, L0SliceDto> producerL0SlicesFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * \ for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "kafkaProductTemplate")
	public KafkaTemplate<String, L0SliceDto> kafkaL0SlicesTemplate() {
		return new KafkaTemplate<>(producerL0SlicesFactory());
	}

	/**
	 * KAFKA producer for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "senderL0Products")
	public L0SlicesProducer senderL0Slices() {
		return new L0SlicesProducer();
	}

	/**
	 * ------------------------------------------------------------------<br/>
	 * PRODUCER L0_ACN <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Producer factory for L0Slices
	 * 
	 * @return
	 */
	@Bean(name = "producerAcnFactory")
	public ProducerFactory<String, L0AcnDto> producerAcnFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * \ for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "kafkaAcnTemplate")
	public KafkaTemplate<String, L0AcnDto> kafkaAcnTemplate() {
		return new KafkaTemplate<>(producerAcnFactory());
	}

	/**
	 * KAFKA producer for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "senderL0Acns")
	public L0ACNsProducer senderL0Acns() {
		return new L0ACNsProducer();
	}

	/**
	 * ------------------------------------------------------------------<br/>
	 * PRODUCER REPORT <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Producer factory for L0Slices
	 * 
	 * @return
	 */
	@Bean(name = "producerL0ReportsFactory")
	public ProducerFactory<String, ReportDto> producerL0ReportsFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * \ for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "kafkaL0ReportTemplate")
	public KafkaTemplate<String, ReportDto> kafkaL0ReportTemplate() {
		return new KafkaTemplate<>(producerL0ReportsFactory());
	}

	/**
	 * KAFKA producer for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "senderL0Reports")
	public L0ReportProducer senderL0Reports() {
		return new L0ReportProducer();
	}

	/**
	 * ------------------------------------------------------------------<br/>
	 * PRODUCER L1_PRODUCT <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Producer factory for L0Slices
	 * 
	 * @return
	 */
	@Bean(name = "producerL1ProductFactory")
	public ProducerFactory<String, L1SliceDto> producerL1SlicesFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * \ for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "kafkaL1ProductTemplate")
	public KafkaTemplate<String, L1SliceDto> kafkaL1SlicesTemplate() {
		return new KafkaTemplate<>(producerL1SlicesFactory());
	}

	/**
	 * KAFKA producer for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "senderL1Products")
	public L1SlicesProducer senderL1Slices() {
		return new L1SlicesProducer();
	}

	/**
	 * ------------------------------------------------------------------<br/>
	 * PRODUCER L0_ACN <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Producer factory for L0Slices
	 * 
	 * @return
	 */
	@Bean(name = "producerL1AcnFactory")
	public ProducerFactory<String, L1AcnDto> producerL1AcnFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * \ for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "kafkaL1AcnTemplate")
	public KafkaTemplate<String, L1AcnDto> kafkaL1AcnTemplate() {
		return new KafkaTemplate<>(producerL1AcnFactory());
	}

	/**
	 * KAFKA producer for L0 Slices
	 * 
	 * @return
	 */
	@Bean(name = "senderL1Acns")
	public L1ACNsProducer senderL1Acns() {
		return new L1ACNsProducer();
	}

	/**
	 * ------------------------------------------------------------------<br/>
	 * PRODUCER L1 REPORT <br/>
	 * ------------------------------------------------------------------<br/>
	 */

	/**
	 * Producer factory for L1 reports
	 * 
	 * @return
	 */
	@Bean(name = "producerL1ReportsFactory")
	public ProducerFactory<String, ReportDto> producerL1ReportsFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigs());
	}

	/**
	 * KAFKA template, the producer wrapper (the producer factory is provided in it)
	 * \ for L1 reports
	 * 
	 * @return
	 */
	@Bean(name = "kafkaL1ReportTemplate")
	public KafkaTemplate<String, ReportDto> kafkaL1ReportTemplate() {
		return new KafkaTemplate<>(producerL1ReportsFactory());
	}

	/**
	 * KAFKA producer for L1 reports
	 * 
	 * @return
	 */
	@Bean(name = "senderL1Reports")
	public L1ReportProducer senderL1Reports() {
		return new L1ReportProducer();
	}
}