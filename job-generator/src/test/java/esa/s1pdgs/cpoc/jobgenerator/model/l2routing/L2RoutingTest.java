package esa.s1pdgs.cpoc.jobgenerator.model.l2routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L2Routing
 * 
 *
 */
public class L2RoutingTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		L2Route route1 = new L2Route(new L2RouteFrom("IW", "S1B"), new L2RouteTo(Arrays.asList("tt1.xml")));
		L2Route route2 = new L2Route(new L2RouteFrom("EM", "S1A"), new L2RouteTo(Arrays.asList("tt2.xml")));
		
		L2Routing obj = new L2Routing();
		obj.addRoute(route1);
		obj.addRoute(route2);
		
		assertTrue(obj.getRoutes().size() == 2);
		assertEquals(route1, obj.getRoutes().get(0));
		assertEquals(route2, obj.getRoutes().get(1));
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		L2Route route1 = new L2Route(new L2RouteFrom("IW", "S1B"), new L2RouteTo(Arrays.asList("tt1.xml")));
		L2Route route2 = new L2Route(new L2RouteFrom("EM", "S1A"), new L2RouteTo(Arrays.asList("tt2.xml")));
		
		L2Routing obj = new L2Routing();
		List<L2Route> routes = Arrays.asList(route1, route2);
		obj.setRoutes(routes);
		
		String str = obj.toString();
		assertTrue(str.contains("routes: " + routes.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(L2Routing.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
