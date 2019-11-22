package esa.s1pdgs.cpoc.compression.trigger.service;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionTriggerTest {

	@Test
	public void testGetCompressedKeyObjectStorage() {
		CompressionTrigger t = new CompressionTrigger(null);

		assertEquals(t.getCompressedProductFamily(ProductFamily.L0_ACN), ProductFamily.L0_ACN_ZIP);
		assertEquals(t.getCompressedProductFamily(ProductFamily.L1_ACN), ProductFamily.L1_ACN_ZIP);
		assertEquals(t.getCompressedProductFamily(ProductFamily.L2_ACN), ProductFamily.L2_ACN_ZIP);
		assertEquals(t.getCompressedProductFamily(ProductFamily.L0_SLICE), ProductFamily.L0_SLICE_ZIP);
		assertEquals(t.getCompressedProductFamily(ProductFamily.L1_SLICE), ProductFamily.L1_SLICE_ZIP);
		assertEquals(t.getCompressedProductFamily(ProductFamily.L2_SLICE), ProductFamily.L2_SLICE_ZIP);
	}
}
