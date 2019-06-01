package esa.s1pdgs.cpoc.jobgenerator.model.l2routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L2Route
 * 
 *
 */
public class L2RouteTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		L2RouteTo to = new L2RouteTo(Arrays.asList("tt1.xml","tt2.xml","tt3.xml"));
		L2RouteFrom from = new L2RouteFrom("IW", "S1B");
		
		L2Route obj = new L2Route(from, to);
		
		assertEquals(from, obj.getRouteFrom());
		assertEquals(to, obj.getRouteTo());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		L2RouteTo to = new L2RouteTo(Arrays.asList("tt1.xml","tt2.xml","tt3.xml"));
		L2RouteFrom from = new L2RouteFrom("IW", "S1B");
		
		L2Route obj = new L2Route();
		obj.setRouteFrom(from);
		obj.setRouteTo(to);
		
		String str = obj.toString();
		assertTrue(str.contains("routeFrom: " + from.toString()));
		assertTrue(str.contains("routeTo: " + to.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(L2Route.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
