/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.xml.model.tasktable.routing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRoute;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteFrom;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouteTo;
import esa.s1pdgs.cpoc.xml.model.tasktable.routing.LevelProductsRouting;
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
