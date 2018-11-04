package esa.s1pdgs.cpoc.scaler.kafka;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.scaler.kafka.KafkaMonitoringProperties;
import esa.s1pdgs.cpoc.scaler.kafka.model.SpdgsTopic;

/**
 * Test the properties of OpenStack
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class KafkaMonitoringPropertiesTest {

    /**
     * Properties to test
     */
    @Autowired
    private KafkaMonitoringProperties properties;

    /**
     * Test the parsing of the properties
     */
    @Test
    public void testInit() {
        assertEquals("spdgs-scaler", properties.getClientId());
        assertEquals("10.2.3.1:9093", properties.getBootstrapServers());
        assertEquals(5000, properties.getSessionTimeoutMs());
        assertEquals(5000, properties.getRequestTimeoutMs());
        assertEquals(660000, properties.getCnxMaxIdlMs());
        assertEquals(1, properties.getTopics().size());
        assertEquals(Arrays.asList("l1-jobs"), properties.getTopics().get(SpdgsTopic.L1_JOBS));
        assertEquals(1, properties.getGroupIdPerTopic().size());
        assertEquals("l1-job-generators",
                properties.getGroupIdPerTopic().get(SpdgsTopic.L1_JOBS));

    }
}
