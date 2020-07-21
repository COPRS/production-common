package esa.s1pdgs.cpoc.xml.model.tasktable.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteFrom;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteTo;
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

		LevelProductsRouteTo to = new LevelProductsRouteTo(Arrays.asList("tt1.xml","tt2.xml","tt3.xml"));
		LevelProductsRouteFrom from = new LevelProductsRouteFrom("IW", "S1B");
		
		LevelProductsRoute obj = new LevelProductsRoute(from, to);
		
		assertEquals(from, obj.getRouteFrom());
		assertEquals(to, obj.getRouteTo());
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		
		LevelProductsRouteTo to = new LevelProductsRouteTo(Arrays.asList("tt1.xml","tt2.xml","tt3.xml"));
		LevelProductsRouteFrom from = new LevelProductsRouteFrom("IW", "S1B");
		
		LevelProductsRoute obj = new LevelProductsRoute();
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
		EqualsVerifier.forClass(LevelProductsRoute.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
