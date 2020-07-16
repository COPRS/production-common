package esa.s1pdgs.cpoc.production.trigger.taskTableMapping;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.appcatalog.AppDataJobProduct;

public class TestConfigurableKeyEvaluator {

	@Test
	public final void testSentinel1DefaultPattern() {
		final ConfigurableKeyEvaluator uut = new ConfigurableKeyEvaluator("${product.swathtype}_${product.satelliteId}");
		final AppDataJobProduct prod = new AppDataJobProduct();
		prod.getMetadata().put("foo", "baaaaar");
		prod.getMetadata().put("swathtype", "WV");
		prod.getMetadata().put("satelliteId", "B");		
		assertEquals("WV_B", uut.apply(prod));
	}
}
