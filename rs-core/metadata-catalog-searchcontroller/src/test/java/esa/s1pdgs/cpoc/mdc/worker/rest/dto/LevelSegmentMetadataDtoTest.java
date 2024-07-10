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

package esa.s1pdgs.cpoc.mdc.worker.rest.dto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.LevelSegmentMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class LevelSegmentMetadataDtoTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		LevelSegmentMetadata obj = new LevelSegmentMetadata("name", "type", "kobs", "start", "stop", "mission", "satellite", "station");
		assertEquals("name", obj.getProductName());
        assertEquals("type", obj.getProductType());
        assertEquals("kobs", obj.getKeyObjectStorage());
        assertEquals("start", obj.getValidityStart());
        assertEquals("stop", obj.getValidityStop());
		
		obj.setDatatakeId("14256");
		obj.setConsolidation("consol");
		obj.setPolarisation("pol");
        assertEquals("14256", obj.getDatatakeId());
        assertEquals("consol", obj.getConsolidation());
        assertEquals("pol", obj.getPolarisation());
		
		String str = obj.toJsonString();
		assertTrue(str.contains("\"productName\":\"name"));
		assertTrue(str.contains("\"productType\":\"type"));
		assertTrue(str.contains("\"keyObjectStorage\":"));
		assertTrue(str.contains("\"validityStart\":\"start"));
		assertTrue(str.contains("\"validityStop\":\"stop"));
		assertTrue(str.contains("\"missionId\":\"mission\""));
		assertTrue(str.contains("\"satelliteId\":\"satellite\""));
		assertTrue(str.contains("\"stationCode\":\"station\""));
		assertTrue(str.contains("\"consolidation\":\"consol"));
		assertTrue(str.contains("\"polarisation\":\"pol"));
		assertTrue(str.contains("\"datatakeId\":\"14256"));
		
		obj.setProductName("name2");
		obj.setProductType("type2");
		obj.setKeyObjectStorage("kobs2");
		obj.setValidityStart("start2");
		obj.setValidityStop("stop2");
        assertEquals("name2", obj.getProductName());
        assertEquals("type2", obj.getProductType());
        assertEquals("kobs2", obj.getKeyObjectStorage());
        assertEquals("start2", obj.getValidityStart());
        assertEquals("stop2", obj.getValidityStop());
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(LevelSegmentMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
