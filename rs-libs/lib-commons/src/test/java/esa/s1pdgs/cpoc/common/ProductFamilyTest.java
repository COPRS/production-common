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

package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;

/**
 * Test the enum ProductFamily
 * @author Cyrielle Gailliard
 *
 */
public class ProductFamilyTest {
    
    /**
     * Test basic enumeration
     */
    @Test
    public void testBasic() {
        assertEquals(ProductFamily.L0_SLICE, ProductFamily.valueOf("L0_SLICE"));
    }

	/**
	 * Test the function fromValue
	 */
	@Test
	public void testFromVamlue() {
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue(null));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue(""));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("edrs_session"));
		assertEquals(ProductFamily.EDRS_SESSION, ProductFamily.fromValue("EDRS_SESSION"));
		assertEquals(ProductFamily.L1_REPORT, ProductFamily.fromValue("L1_REPORT"));
		assertEquals(ProductFamily.L2_REPORT, ProductFamily.fromValue("L2_REPORT"));
		assertEquals(ProductFamily.BLANK, ProductFamily.fromValue("L1_REPORTd"));
	}
}
