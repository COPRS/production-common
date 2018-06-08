package fr.viveris.s1pdgs.jobgenerator.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputMode;

/**
 * Test the enum ProductMode
 * @author Cyrielle Gailliard
 *
 */
public class ProductModeTest {
	
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
