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

package esa.s1pdgs.cpoc.preparation.worker.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.enums.TaskTableInputMode;

/**
 * Test the enum ProductMode
 * @author Cyrielle Gailliard
 *
 */
public class ProductModeTest {
    
    @Test
    public void testBasic() {
        assertEquals(7, ProductMode.values().length);
        assertEquals(ProductMode.ALWAYS, ProductMode.valueOf("ALWAYS"));
        assertEquals(ProductMode.BLANK, ProductMode.valueOf("BLANK"));
        assertEquals(ProductMode.NON_SLICING, ProductMode.valueOf("NON_SLICING"));
        assertEquals(ProductMode.SLICING, ProductMode.valueOf("SLICING"));
        assertEquals(ProductMode.NRT, ProductMode.valueOf("NRT"));
        assertEquals(ProductMode.STC, ProductMode.valueOf("STC"));
        assertEquals(ProductMode.NTC, ProductMode.valueOf("NTC"));
    }
	
	/**
	 * Test the function isCompatibleWithTaskTableMode
	 */
	@Test
	public void testIsCompatibleWithTaskTableMode() {
		
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(null, TaskTableInputMode.ALWAYS));
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.ALWAYS, null));
		
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.ALWAYS, TaskTableInputMode.ALWAYS));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.ALWAYS, TaskTableInputMode.BLANK));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.ALWAYS, TaskTableInputMode.NON_SLICING));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.ALWAYS, TaskTableInputMode.SLICING));

		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.ALWAYS, TaskTableInputMode.ALWAYS));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.NON_SLICING, TaskTableInputMode.ALWAYS));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.SLICING, TaskTableInputMode.ALWAYS));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.BLANK, TaskTableInputMode.ALWAYS));

		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.NON_SLICING, TaskTableInputMode.NON_SLICING));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.BLANK, TaskTableInputMode.BLANK));
		assertTrue(ProductMode.isCompatibleWithTaskTableMode(ProductMode.SLICING, TaskTableInputMode.SLICING));
		
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.NON_SLICING, TaskTableInputMode.SLICING));
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.NON_SLICING, TaskTableInputMode.BLANK));
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.SLICING, TaskTableInputMode.NON_SLICING));
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.SLICING, TaskTableInputMode.BLANK));
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.BLANK, TaskTableInputMode.SLICING));
		assertFalse(ProductMode.isCompatibleWithTaskTableMode(ProductMode.BLANK, TaskTableInputMode.NON_SLICING));
		
	}
}
