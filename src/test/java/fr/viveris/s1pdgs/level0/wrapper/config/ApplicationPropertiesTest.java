package fr.viveris.s1pdgs.level0.wrapper.config;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import fr.viveris.s1pdgs.level0.wrapper.model.ApplicationLevel;

/**
 * Check the application properties
 * @author Viveris Technologies
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationPropertiesTest {

    /**
     * Embedded Kafka
     */
    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, "t-pdgs-l0-jobs");
    
    /**
     * Properties to test
     */
    @Autowired
    private ApplicationProperties properties;

    /**
     * Test the properties
     */
    @Test
    public void testLoadProperties() {
        assertEquals(ApplicationLevel.L0, properties.getLevel());
        assertEquals(1800, properties.getTmProcAllTasksS());
        assertEquals(600, properties.getTmProcOneTaskS());
        assertEquals(300, properties.getTmProcStopS());
        assertEquals(60, properties.getTmProcCheckStopS());
        assertEquals(20, properties.getSizeBatchUpload());
        assertEquals(5, properties.getSizeBatchDownload());
        assertEquals(12, properties.getWapNbMaxLoop());
        assertEquals(10, properties.getWapTempoS());
    }
}
