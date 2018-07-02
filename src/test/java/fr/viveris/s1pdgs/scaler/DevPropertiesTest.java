package fr.viveris.s1pdgs.scaler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Test the properties of development
 * 
 * @author Viveris Technologies
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class DevPropertiesTest {

    /**
     * Properties to test
     */
    @Autowired
    private DevProperties properties;

    /**
     * Test the parsing of the properties
     */
    @Test
    public void testInit() {
        // Check global properties
        assertEquals(7, properties.getActivations().size());
        assertTrue(properties.getActivations().get("pod-deletion"));
        assertTrue(properties.getActivations().get("kafka-monitoring"));
        assertTrue(properties.getActivations().get("init-scaling"));
        assertTrue(properties.getActivations().get("k8s-monitoring"));
        assertTrue(properties.getActivations().get("value-monitored"));
        assertFalse(properties.getActivations().get("scaling"));
        assertFalse(
                properties.getActivations().get("unused-ressources-deletion"));
    }

}
