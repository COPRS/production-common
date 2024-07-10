/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
