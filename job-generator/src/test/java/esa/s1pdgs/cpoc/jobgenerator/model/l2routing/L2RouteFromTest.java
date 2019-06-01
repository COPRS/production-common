package esa.s1pdgs.cpoc.jobgenerator.model.l2routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L2RouteFrom
 * 
 *
 */
public class L2RouteFromTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		
		L2RouteFrom obj = new L2RouteFrom("WV", "S1B");
		
		assertEquals("WV", obj.getAcquisition());
		assertEquals("S1B", obj.getSatelliteId());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		L2RouteFrom obj = new L2RouteFrom();
		obj.setAcquisition("IW");
		obj.setSatelliteId("S1B");
		
		String str = obj.toString();
		assertTrue(str.contains("acquisition: IW"));
		assertTrue(str.contains("satelliteId: S1B"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(L2RouteFrom.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
