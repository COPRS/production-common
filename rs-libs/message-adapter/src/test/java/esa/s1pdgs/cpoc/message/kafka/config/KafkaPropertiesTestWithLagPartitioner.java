package esa.s1pdgs.cpoc.message.kafka.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


/**
 * Check the initialization of the kafka properties
 *
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@ActiveProfiles("withlagpartitioner")
public class KafkaPropertiesTestWithLagPartitioner {

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

        // Producer
        assertEquals(10, properties.getProducer().getMaxRetries());
        assertEquals(new Integer(180), properties.getProducer().getLagBasedPartitioner().getDelaySeconds());
        assertEquals("compression-worker", properties.getProducer().getLagBasedPartitioner().getConsumerGroup());

        assertEquals(new Integer(10), properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().get("t-pdgs-compression-jobs-nrt"));
        assertEquals(new Integer(5), properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().get("t-pdgs-compression-jobs-fast"));
        assertEquals(new Integer(20), properties.getProducer().getLagBasedPartitioner().getTopicsWithPriority().get("t-pdgs-compression-jobs-pt"));
    }
}
