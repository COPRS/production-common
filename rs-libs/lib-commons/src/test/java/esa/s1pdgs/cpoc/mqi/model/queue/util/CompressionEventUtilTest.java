package esa.s1pdgs.cpoc.mqi.model.queue.util;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;

public class CompressionEventUtilTest {

	@Test
	public void testGetCompressedKeyObjectStorage() {
		assertEquals(CompressionEventUtil.composeCompressedProductFamily(ProductFamily.L0_ACN),
				ProductFamily.L0_ACN_ZIP);
		assertEquals(CompressionEventUtil.composeCompressedProductFamily(ProductFamily.L1_ACN),
				ProductFamily.L1_ACN_ZIP);
		assertEquals(CompressionEventUtil.composeCompressedProductFamily(ProductFamily.L2_ACN),
				ProductFamily.L2_ACN_ZIP);
		assertEquals(CompressionEventUtil.composeCompressedProductFamily(ProductFamily.L0_SLICE),
				ProductFamily.L0_SLICE_ZIP);
		assertEquals(CompressionEventUtil.composeCompressedProductFamily(ProductFamily.L1_SLICE),
				ProductFamily.L1_SLICE_ZIP);
		assertEquals(CompressionEventUtil.composeCompressedProductFamily(ProductFamily.L2_SLICE),
				ProductFamily.L2_SLICE_ZIP);
	}

	@Test
	public final void testRemoveZipSuffix() {
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.Zip"));
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.ZIP"));
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.zip"));
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.tgz"));
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.tar.gz"));
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.tAr"));		
		assertEquals("foo.bar", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar"));		
		assertEquals("foo.bar.tar_gz", CompressionEventUtil.removeZipFromKeyObjectStorage("foo.bar.tar_gz"));
	}
	
	@Test
	public final void testRemoveZipSuffixFromProductFamily() {
		assertEquals(ProductFamily.L0_ACN, CompressionEventUtil.removeZipSuffixFromProductFamily(ProductFamily.L0_ACN_ZIP));
		assertEquals(ProductFamily.L1_SLICE, CompressionEventUtil.removeZipSuffixFromProductFamily(ProductFamily.L1_SLICE_ZIP));
	}

}
