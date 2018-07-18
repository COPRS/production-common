package esa.s1pdgs.cpoc.mqi.server;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties.KafkaConsumerProperties;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties.KafkaListenerProperties;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties.KafkaProducerProperties;

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
    public static KafkaEmbedded embeddedKafka =
            new KafkaEmbedded(1, true, "t-pdgs-l0-jobs");

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
        assertEquals("wrapper", properties.getClientId());
        assertEquals("t-pdgs-errors", properties.getErrorTopic());

        // Consumer
        assertEquals("wrappers", properties.getConsumer().getGroupId());
        assertEquals(3000, properties.getConsumer().getHeartbeatIntvMs());
        assertEquals(3600000, properties.getConsumer().getMaxPollIntervalMs());
        assertEquals(1, properties.getConsumer().getMaxPollRecords());
        assertEquals(10000, properties.getConsumer().getSessionTimeoutMs());
        assertEquals("latest", properties.getConsumer().getAutoOffsetReset());

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
        KafkaProducerProperties producer = new KafkaProducerProperties();
        producer.setMaxRetries(5);
        KafkaListenerProperties listener = new KafkaListenerProperties();
        listener.setPollTimeoutMs(50);
        KafkaConsumerProperties consumer = new KafkaConsumerProperties();
        consumer.setGroupId("group-id");
        consumer.setHeartbeatIntvMs(1);
        consumer.setMaxPollIntervalMs(2);
        consumer.setMaxPollRecords(3);
        consumer.setSessionTimeoutMs(4);
        consumer.setAutoOffsetReset("earliest");

        properties.setListener(listener);
        properties.setProducer(producer);
        properties.setConsumer(consumer);
        properties.setBootstrapServers("url:port");
        properties.setClientId("client-id");

        // General
        assertEquals("url:port", properties.getBootstrapServers());
        assertEquals("client-id", properties.getClientId());

        // Consumer
        assertEquals("group-id", properties.getConsumer().getGroupId());
        assertEquals(1, properties.getConsumer().getHeartbeatIntvMs());
        assertEquals(2, properties.getConsumer().getMaxPollIntervalMs());
        assertEquals(3, properties.getConsumer().getMaxPollRecords());
        assertEquals(4, properties.getConsumer().getSessionTimeoutMs());
        assertEquals("earliest", properties.getConsumer().getAutoOffsetReset());

        // Listener
        assertEquals(50, properties.getListener().getPollTimeoutMs());

        // Producer
        assertEquals(5, properties.getProducer().getMaxRetries());
    }

}
