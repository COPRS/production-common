package esa.s1pdgs.cpoc.mqi.server.config;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties.KafkaConsumerProperties;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties.KafkaListenerProperties;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties.KafkaProducerProperties;

/**
 * Check the initialization of the kafka properties
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class KafkaPropertiesTest {

    /**
     * Embedded Kafka
     */
    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka =
            new EmbeddedKafkaRule(1, true, "t-pdgs-aio-execution-jobs");

    /**
     * Properties to test
     */
    @Autowired
    private KafkaProperties properties;

    /**
     * Test the initialization
     */
    @Test
    public void testInitialization() {
        assertEquals("mqi-0", properties.getHostname());
        assertEquals("mqi-server", properties.getClientId());
        assertEquals("t-pdgs-errors", properties.getErrorTopic());

        // Consumer
        assertEquals("wrappers", properties.getConsumer().getGroupId());
        assertEquals(3000, properties.getConsumer().getHeartbeatIntvMs());
        assertEquals(3600000, properties.getConsumer().getMaxPollIntervalMs());
        assertEquals(1, properties.getConsumer().getMaxPollRecords());
        assertEquals(10000, properties.getConsumer().getSessionTimeoutMs());
        assertEquals("latest", properties.getConsumer().getAutoOffsetReset());
        assertEquals(-2, properties.getConsumer().getOffsetDftMode());

        // Listener
        assertEquals(500, properties.getListener().getPollTimeoutMs());

        // Producer
        assertEquals(10, properties.getProducer().getMaxRetries());
    }

    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        final KafkaProducerProperties producer = new KafkaProducerProperties();
        producer.setMaxRetries(5);
        final KafkaListenerProperties listener = new KafkaListenerProperties();
        listener.setPollTimeoutMs(50);
        final KafkaConsumerProperties consumer = new KafkaConsumerProperties();
        consumer.setGroupId("group-id");
        consumer.setHeartbeatIntvMs(1);
        consumer.setMaxPollIntervalMs(2);
        consumer.setMaxPollRecords(3);
        consumer.setSessionTimeoutMs(4);
        consumer.setAutoOffsetReset("earliest");
        consumer.setOffsetDftMode(-1);

        properties.setListener(listener);
        properties.setProducer(producer);
        properties.setConsumer(consumer);
        properties.setBootstrapServers("url:port");
        properties.setErrorTopic("test-error-topic");
        properties.setClientId("client-id");
        properties.setHostname("host-test");

        // General
        assertEquals("url:port", properties.getBootstrapServers());
        assertEquals("client-id", properties.getClientId());
        assertEquals("host-test", properties.getHostname());
        assertEquals("test-error-topic", properties.getErrorTopic());

        // Consumer
        assertEquals("group-id", properties.getConsumer().getGroupId());
        assertEquals(1, properties.getConsumer().getHeartbeatIntvMs());
        assertEquals(2, properties.getConsumer().getMaxPollIntervalMs());
        assertEquals(3, properties.getConsumer().getMaxPollRecords());
        assertEquals(4, properties.getConsumer().getSessionTimeoutMs());
        assertEquals("earliest", properties.getConsumer().getAutoOffsetReset());
        assertEquals(-1, properties.getConsumer().getOffsetDftMode());

        // Listener
        assertEquals(50, properties.getListener().getPollTimeoutMs());

        // Producer
        assertEquals(5, properties.getProducer().getMaxRetries());
    }

}
