package esa.s1pdgs.cpoc.jobgenerator.model.l1routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1Route;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1RouteFrom;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1RouteTo;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L1Route
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L1RouteTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		L1RouteTo to = new L1RouteTo(Arrays.asList("tt1.xml","tt2.xml","tt3.xml"));
		L1RouteFrom from = new L1RouteFrom("IW", "S1B");
		
		L1Route obj = new L1Route(from, to);
		
		assertEquals(from, obj.getRouteFrom());
		assertEquals(to, obj.getRouteTo());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		L1RouteTo to = new L1RouteTo(Arrays.asList("tt1.xml","tt2.xml","tt3.xml"));
		L1RouteFrom from = new L1RouteFrom("IW", "S1B");
		
		L1Route obj = new L1Route();
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
		EqualsVerifier.forClass(L1Route.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
