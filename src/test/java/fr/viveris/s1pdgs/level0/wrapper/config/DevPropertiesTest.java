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

/**
 * Check the application properties
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class DevPropertiesTest {

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
    private DevProperties properties;

    /**
     * Test the properties
     */
    @Test
    public void testLoadProperties() {
        assertEquals(4, properties.getStepsActivation().size());
        assertEquals(true, properties.getStepsActivation().get("download"));
        assertEquals(true, properties.getStepsActivation().get("execution"));
        assertEquals(true, properties.getStepsActivation().get("upload"));
        assertEquals(true, properties.getStepsActivation().get("erasing"));
    }

}
