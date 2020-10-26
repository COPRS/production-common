package esa.s1pdgs.cpoc.message.kafka.config;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.message.MessageConsumerFactory;


/**
 * Check the initialization of the kafka properties
 *
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@SpringBootApplication
public class KafkaPropertiesTest {

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
        assertEquals(new Integer(180), properties.getProducer().getLagBasedPartitioner().getDelaySeconds());
        assertEquals("compression-worker", properties.getProducer().getLagBasedPartitioner().getConsumerGroup());

        assertEquals(new Integer(10), properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().get("t-pdgs-compression-jobs-nrt"));
        assertEquals(new Integer(5), properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().get("t-pdgs-compression-jobs-fast"));
        assertEquals(new Integer(20), properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().get("t-pdgs-compression-jobs-pt"));
    }

    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        final KafkaProperties.KafkaProducerProperties producer = new KafkaProperties.KafkaProducerProperties();
        producer.setMaxRetries(5);
        final KafkaProperties.KafkaListenerProperties listener = new KafkaProperties.KafkaListenerProperties();
        listener.setPollTimeoutMs(50);
        final KafkaProperties.KafkaConsumerProperties consumer = new KafkaProperties.KafkaConsumerProperties();
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

    @Configuration
    public static class TestConfig {
        @Bean
        public MessageConsumerFactory<Object> emptyFactory() {
            return Collections::emptyList;
        }
    }
}
