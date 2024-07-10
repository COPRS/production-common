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
