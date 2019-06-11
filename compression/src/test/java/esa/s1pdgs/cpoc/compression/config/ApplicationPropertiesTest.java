package esa.s1pdgs.cpoc.compression.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationPropertiesTest {
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
        assertEquals("/usr/bin/sh", properties.getCommand());
    }
}
