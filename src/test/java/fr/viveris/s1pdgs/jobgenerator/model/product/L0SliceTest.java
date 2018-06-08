package fr.viveris.s1pdgs.jobgenerator.model.product;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object EdrsSessionProduct
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L0SliceTest {

	/**
	 * Check toString
	 */
	@Test
	public void testToString() {
		L0Slice obj = new L0Slice("IW");
		obj.setDataTakeId("123456");
		obj.setNumberSlice(3);
		obj.setTotalNbOfSlice(8);
		obj.setSegmentStartDate("start_date");
		obj.setSegmentStopDate("stop_date");
		
		assertEquals("IW", obj.getAcquisition());
		assertEquals("123456", obj.getDataTakeId());
		assertEquals(3, obj.getNumberSlice());
		assertEquals(8, obj.getTotalNbOfSlice());
		assertEquals("start_date", obj.getSegmentStartDate());
		assertEquals("stop_date", obj.getSegmentStopDate());

		String str = obj.toString();
		assertTrue(str.contains("acquisition: IW"));
		assertTrue(str.contains("dataTakeId: 123456"));
		assertTrue(str.contains("numberSlice: 3"));
		assertTrue(str.contains("totalNbOfSlice: 8"));
		assertTrue(str.contains("segmentStartDate: start_date"));
		assertTrue(str.contains("segmentStopDate: stop_date"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0Slice.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
