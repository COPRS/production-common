package esa.s1pdgs.cpoc.queuewatcher.mqi;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MqiConfigurationTest {

	 @Autowired
	    private MqiConfiguration mqiConfiguration;

	    @Test
	    public void testSettings() {
	        assertEquals(
	                "http://localhost:8081",
	                this.mqiConfiguration.getHostUri());
	    }

}
