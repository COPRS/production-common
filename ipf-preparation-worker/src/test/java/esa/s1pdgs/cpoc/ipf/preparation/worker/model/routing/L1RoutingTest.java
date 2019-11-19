package esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing.LevelProductsRouteFrom;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing.LevelProductsRouteTo;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.routing.LevelProductsRouting;
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

		LevelProductsRoute route1 = new LevelProductsRoute(new LevelProductsRouteFrom("IW", "S1B"), new LevelProductsRouteTo(Arrays.asList("tt1.xml")));
		LevelProductsRoute route2 = new LevelProductsRoute(new LevelProductsRouteFrom("EM", "S1A"), new LevelProductsRouteTo(Arrays.asList("tt2.xml")));
		
		LevelProductsRouting obj = new LevelProductsRouting();
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
		
		LevelProductsRoute route1 = new LevelProductsRoute(new LevelProductsRouteFrom("IW", "S1B"), new LevelProductsRouteTo(Arrays.asList("tt1.xml")));
		LevelProductsRoute route2 = new LevelProductsRoute(new LevelProductsRouteFrom("EM", "S1A"), new LevelProductsRouteTo(Arrays.asList("tt2.xml")));
		
		LevelProductsRouting obj = new LevelProductsRouting();
		List<LevelProductsRoute> routes = Arrays.asList(route1, route2);
		obj.setRoutes(routes);
		
		String str = obj.toString();
		assertTrue(str.contains("routes: " + routes.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void equalsDto() {
		EqualsVerifier.forClass(LevelProductsRouting.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
