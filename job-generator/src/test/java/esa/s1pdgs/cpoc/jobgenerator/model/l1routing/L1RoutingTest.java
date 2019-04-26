package esa.s1pdgs.cpoc.jobgenerator.model.l1routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1Route;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1RouteFrom;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1RouteTo;
import esa.s1pdgs.cpoc.jobgenerator.model.l1routing.L1Routing;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object L1Routing
 * 
 * @author Cyrielle Gailliard
 *
 */
public class L1RoutingTest {
	
	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		L1Route route1 = new L1Route(new L1RouteFrom("IW", "S1B"), new L1RouteTo(Arrays.asList("tt1.xml")));
		L1Route route2 = new L1Route(new L1RouteFrom("EM", "S1A"), new L1RouteTo(Arrays.asList("tt2.xml")));
		
		L1Routing obj = new L1Routing();
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
		
		L1Route route1 = new L1Route(new L1RouteFrom("IW", "S1B"), new L1RouteTo(Arrays.asList("tt1.xml")));
		L1Route route2 = new L1Route(new L1RouteFrom("EM", "S1A"), new L1RouteTo(Arrays.asList("tt2.xml")));
		
		L1Routing obj = new L1Routing();
		List<L1Route> routes = Arrays.asList(route1, route2);
		obj.setRoutes(routes);
		
		String str = obj.toString();
		assertTrue(str.contains("routes: " + routes.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(L1Routing.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
