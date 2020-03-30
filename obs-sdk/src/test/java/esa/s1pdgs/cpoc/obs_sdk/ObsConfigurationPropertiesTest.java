package esa.s1pdgs.cpoc.obs_sdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ObsConfigurationProperties.class})
public class ObsConfigurationPropertiesTest {
	
	@Autowired
	private ObsConfigurationProperties uut;
	
	@Test
	public final void testConfigurationLoading() {
		// only spot check some configurations
		assertEquals("http://oss.eu-west-0.prod-cloud-ocb.orange-business.com/", uut.getEndpoint());
		assertEquals("werum-ut-session-files", uut.getBucket().get(ProductFamily.EDRS_SESSION));
		assertEquals("aws-s3", uut.getBackend());
	}
	
}
