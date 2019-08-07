package esa.s1pdgs.cpoc.disseminator;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.DisseminationTypeConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration;
import esa.s1pdgs.cpoc.disseminator.config.DisseminationProperties.OutboxConfiguration.Protocol;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class ApplicationTest {
	
	@Autowired
	private DisseminationProperties properties;
	
	@Test
	public final void testDisseminationProperties() {
		// check that properties have been loaded successfully by spot checking some elements
		
		System.out.println(properties);
		
		assertEquals("localhost", properties.getHostname());
		
		final Map<String, OutboxConfiguration> outboxes = properties.getOutboxes();
		assertEquals(1, outboxes.size());		
		final Map.Entry<String, OutboxConfiguration> entry = outboxes.entrySet().iterator().next();
		assertEquals("myLittleOutbox", entry.getKey());		
		final OutboxConfiguration outbox = entry.getValue();
		assertEquals(Protocol.FILE, outbox.getProtocol());
		assertEquals("/tmp/foo/bar", outbox.getPath());
		
		final Map<ProductCategory, List<DisseminationTypeConfiguration>> catConfig = properties.getCategories();
		assertEquals(1, catConfig.size());			
		final Map.Entry<ProductCategory, List<DisseminationTypeConfiguration>> catEntry = catConfig.entrySet().iterator().next();
		assertEquals(ProductCategory.AUXILIARY_FILES, catEntry.getKey());		
		final List<DisseminationTypeConfiguration> diss = catEntry.getValue();
		assertEquals(1, diss.size());
		final DisseminationTypeConfiguration dissConfig = diss.get(0);
		assertEquals("^([0-9a-z][0-9a-z]){1}([0-9a-z_]){1}(_(OPER|TEST))?_(AUX_OBMEMC|AUX_PP1|AUX_PP2|AUX_CAL|AUX_INS|AUX_RESORB|AUX_WND|AUX_ICE|AUX_WAV|MPL_ORBPRE|MPL_ORBSCT)_\\w{1,}\\.(XML|EOF|SAFE)(/.*)?$", dissConfig.getRegex());
		assertEquals("myLittleOutbox", dissConfig.getTarget());
	}
}
