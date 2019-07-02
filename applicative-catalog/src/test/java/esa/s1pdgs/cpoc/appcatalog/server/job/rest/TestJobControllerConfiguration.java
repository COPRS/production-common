package esa.s1pdgs.cpoc.appcatalog.server.job.rest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.appcatalog.server.job.rest.JobControllerConfiguration;
import esa.s1pdgs.cpoc.common.ProductCategory;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
@EnableConfigurationProperties(JobControllerConfiguration.class)
public class TestJobControllerConfiguration {
	
	@Autowired
	private JobControllerConfiguration config;

	@Test
	public final void testConfigurationMapping()
	{
		assertEquals(52, config.getFor(ProductCategory.LEVEL_SEGMENTS).getMaxErrorsInitial());
		assertEquals(50, config.getFor(ProductCategory.LEVEL_PRODUCTS).getMaxErrorsInitial());
		assertEquals(51, config.getFor(ProductCategory.EDRS_SESSIONS).getMaxErrorsInitial());
		
		assertEquals(303, config.getFor(ProductCategory.LEVEL_SEGMENTS).getMaxErrorsPrimaryCheck());
		assertEquals(301, config.getFor(ProductCategory.LEVEL_PRODUCTS).getMaxErrorsPrimaryCheck());
		assertEquals(302, config.getFor(ProductCategory.EDRS_SESSIONS).getMaxErrorsPrimaryCheck());
	}
}
