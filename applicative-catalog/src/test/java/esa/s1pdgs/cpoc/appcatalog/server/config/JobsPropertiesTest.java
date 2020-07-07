package esa.s1pdgs.cpoc.appcatalog.server.config;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.server.config.JobsProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class JobsPropertiesTest {

    @Autowired
    private JobsProperties properties;
    
    @Test
    public void testInitValues() {
        assertEquals(600000, properties.getCleaningJobsTerminatedFixedRateMs());
        assertEquals(18000000, properties.getCleaningJobsInvalidFixedRateMs());
        assertEquals(4, properties.getLevelProducts().getMaxAgeJobMs().size());
        assertEquals(Long.valueOf(600000L), properties.getLevelProducts().getMaxAgeJobMs().get("waiting"));
        assertEquals(Long.valueOf(3600000L), properties.getLevelProducts().getMaxAgeJobMs().get("dispatching"));
        assertEquals(Long.valueOf(3600000L), properties.getLevelProducts().getMaxAgeJobMs().get("generating"));
        assertEquals(Long.valueOf(600000L), properties.getLevelProducts().getMaxAgeJobMs().get("terminated"));
        assertEquals(4, properties.getEdrsSessions().getMaxAgeJobMs().size());
        assertEquals(Long.valueOf(25200000L), properties.getEdrsSessions().getMaxAgeJobMs().get("waiting"));
        assertEquals(Long.valueOf(3600000L), properties.getEdrsSessions().getMaxAgeJobMs().get("dispatching"));
        assertEquals(Long.valueOf(3600000L), properties.getEdrsSessions().getMaxAgeJobMs().get("generating"));
        assertEquals(Long.valueOf(600000L), properties.getEdrsSessions().getMaxAgeJobMs().get("terminated"));
        
        properties.setCleaningJobsInvalidFixedRateMs(12);
        properties.setCleaningJobsTerminatedFixedRateMs(14);
        assertEquals(14, properties.getCleaningJobsTerminatedFixedRateMs());
        assertEquals(12, properties.getCleaningJobsInvalidFixedRateMs());
    }
}
