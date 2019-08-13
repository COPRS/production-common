package esa.s1pdgs.cpoc.validation.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductFamily;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ConfigTest {

	@Autowired
	private ApplicationProperties properties;
	
	@Test
	public void testFamilies() {	
		// Verify that the amount of entries are read
		assertEquals(2,properties.getFamilies().size());
		
		// Verify that the values from L0 Slice are read correctly
		assertEquals(60,properties.getFamilies().get(ProductFamily.L0_SLICE).getInitialDelay());
	}
}
