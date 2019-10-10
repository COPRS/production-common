package esa.s1pdgs.cpoc.archives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Ignore;
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
@Ignore
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
        assertEquals(1, properties.getActivations().size());
        assertFalse(properties.getActivations().get("download-all"));
    }

}
