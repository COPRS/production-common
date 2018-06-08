package fr.viveris.s1pdgs.jobgenerator.model.product;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import fr.viveris.s1pdgs.jobgenerator.model.ProductMode;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object EdrsSessionProduct
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceProductTest {

	/**
	 * Check toString
	 */
	@Test
	public void testToString() {
		Date startTime = new Date(System.currentTimeMillis() - 10000);
		Date stopTime = new Date(System.currentTimeMillis());
		L0Slice slice = new L0Slice("IW");
		L0SliceProduct obj = new L0SliceProduct("id", "A", "S1", startTime, stopTime, slice);
		obj.setInsConfId(4);
		obj.setMode(ProductMode.NON_SLICING);

		String str = obj.toString();
		assertTrue(str.contains("identifier: id"));
		assertTrue(str.contains("satelliteId: A"));
		assertTrue(str.contains("missionId: S1"));
		assertTrue(str.contains("startTime: " + startTime.toString()));
		assertTrue(str.contains("stopTime: " + stopTime.toString()));
		assertTrue(str.contains("object: " + slice.toString()));
		assertTrue(str.contains("insConfId: 4"));
		assertTrue(str.contains("mode: NON_SLICING"));
		assertTrue(str.contains("productType: "));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0SliceProduct.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
