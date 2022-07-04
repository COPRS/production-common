package esa.s1pdgs.cpoc.message.kafka.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;


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

        // Producer
        assertEquals(10, properties.getProducer().getMaxRetries());
    }

    /**
     * Test setters
     */
    @Test
    public void testSetters() {
        final KafkaProperties.KafkaProducerProperties producer = new KafkaProperties.KafkaProducerProperties();
        producer.setMaxRetries(5);

        properties.setProducer(producer);
        properties.setBootstrapServers("url:port");
        properties.setErrorTopic("test-error-topic");
        properties.setClientId("client-id");
        properties.setHostname("host-test");

        // General
        assertEquals("url:port", properties.getBootstrapServers());
        assertEquals("client-id", properties.getClientId());
        assertEquals("host-test", properties.getHostname());
        assertEquals("test-error-topic", properties.getErrorTopic());

        // Producer
        assertEquals(5, properties.getProducer().getMaxRetries());
    }
}
