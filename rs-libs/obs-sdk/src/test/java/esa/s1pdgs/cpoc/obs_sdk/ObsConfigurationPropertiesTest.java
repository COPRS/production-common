package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(value = ObsConfigurationProperties.class)
@PropertySource({"classpath:obs-aws-s3.properties"})
public class ObsConfigurationPropertiesTest {
	
	@Autowired
	private ObsConfigurationProperties uut;
	
	@Test
	public final void testConfigurationLoading() {
		// only spot check some configurations
		assertEquals("http://storage.gra.cloud.ovh.net", uut.getEndpoint());
		assertEquals("werum-ut-session-files", uut.getBucket().get(ProductFamily.EDRS_SESSION));
		assertEquals("aws-s3", uut.getBackend());
		assertEquals(true, uut.getDisableChunkedEncoding());
	}
	
}
