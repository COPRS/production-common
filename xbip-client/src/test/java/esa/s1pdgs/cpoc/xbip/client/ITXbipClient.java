package esa.s1pdgs.cpoc.xbip.client;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.xbip.client.config.XbipClientConfigurationProperties;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(value = XbipClientConfigurationProperties.class)
public class ITXbipClient {
	
	@Autowired
	private XbipClientConfigurationProperties config;
	
	@Test
	public final void testConfig() {
		System.out.println(config);
	}
}
