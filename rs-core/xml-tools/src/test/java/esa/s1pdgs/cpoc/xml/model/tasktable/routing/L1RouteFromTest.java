package esa.s1pdgs.cpoc.xml.model.tasktable.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteFrom;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L1RouteFrom
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L1RouteFromTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		
		LevelProductsRouteFrom obj = new LevelProductsRouteFrom("IW", "S1B");
		
		assertEquals("IW", obj.getAcquisition());
		assertEquals("S1B", obj.getSatelliteId());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		LevelProductsRouteFrom obj = new LevelProductsRouteFrom();
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
		EqualsVerifier.forClass(LevelProductsRouteFrom.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
