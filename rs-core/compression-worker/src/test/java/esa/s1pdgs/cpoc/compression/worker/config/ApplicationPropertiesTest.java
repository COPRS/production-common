package esa.s1pdgs.cpoc.compression.worker.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.compression.worker.config.ApplicationProperties;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@EnableConfigurationProperties
public class ApplicationPropertiesTest {
    /**
     * Properties to test
     */
    @Autowired
    private ApplicationProperties properties;
    
    @Autowired 
    private ObsClient obsClient;
    
    /**
     * Test the properties
     */
    @Test
    public void testLoadProperties() {
        assertEquals("/usr/bin/sh", properties.getCompressionCommand());
    } 
    
    @Test
    public final void testObsClient() {
    	assertEquals(true, obsClient instanceof S3ObsClient);
    }
}
