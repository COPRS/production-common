package fr.viveris.s1pdgs.ingestor.services.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.springframework.kafka.test.assertj.KafkaConditions.key;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.listener.config.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.ingestor.model.dto.KafkaConfigFileDto;
import fr.viveris.s1pdgs.ingestor.model.exception.KafkaMetadataPublicationException;

/**
 * Test the producer of metadata
 * 
 * @author Cyrielle Gailliard
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class KafkaMetadataProducerTest {

	// Logger
	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaMetadataProducerTest.class);

	// Topic
	private final static String SENDER_TOPIC = "t-pdgs-config-files";

	// KAFKA producer
	@Autowired
	private KafkaMetadataProducer senderMetadata;

	// KAFKA simulated consumer
	private KafkaMessageListenerContainer<String, KafkaConfigFileDto> container;
	// records
	private BlockingQueue<ConsumerRecord<String, KafkaConfigFileDto>> records;

	// Embedded KAFKA
	@ClassRule
	public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, SENDER_TOPIC);

	/**
	 * Test set up
	 * 
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		// set up the Kafka consumer properties
		Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("sender", "false", embeddedKafka);
		consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

		// create a Kafka consumer factory
		DefaultKafkaConsumerFactory<String, KafkaConfigFileDto> consumerFactory = new DefaultKafkaConsumerFactory<String, KafkaConfigFileDto>(
				consumerProperties, new StringDeserializer(), new JsonDeserializer<>(KafkaConfigFileDto.class));

		// set the topic that needs to be consumed
		ContainerProperties containerProperties = new ContainerProperties(SENDER_TOPIC);

		// create a Kafka MessageListenerContainer
		container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

		// create a thread safe queue to store the received message
		records = new LinkedBlockingQueue<>();

		// setup a Kafka message listener
		container.setupMessageListener(new MessageListener<String, KafkaConfigFileDto>() {
			@Override
			public void onMessage(ConsumerRecord<String, KafkaConfigFileDto> record) {
				LOGGER.debug("test-listener received message={}", record.toString());
				records.add(record);
			}
		});

		// start the container and underlying message listener
		container.start();

		// wait until the container has the required number of assigned partitions
		ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
	}

	/**
	 * Test tear down
	 */
	@After
	public void tearDown() {
		// stop the container
		container.stop();
	}

	/**
	 * Test that producer send message in KAFKA
	 * 
	 * @throws InterruptedException
	 * @throws JSONException
	 */
	@Test
	public void testSend() throws InterruptedException, JSONException {
		// send the message
		KafkaConfigFileDto metadata = new KafkaConfigFileDto();
		metadata.setMetadataToIndex(null);
		try {
			senderMetadata.send(metadata);
		} catch (KafkaMetadataPublicationException e) {
			assertFalse("Exception occurred " + e.getMessage(), false);
		}
		// check that the message was received
		ConsumerRecord<String, KafkaConfigFileDto> received = records.poll(10, TimeUnit.SECONDS);
		// Hamcrest Matchers to check the value
		assertThat(received.value()).isEqualTo(metadata);
		// AssertJ Condition to check the key
		assertThat(received).has(key(null));
	}
}
