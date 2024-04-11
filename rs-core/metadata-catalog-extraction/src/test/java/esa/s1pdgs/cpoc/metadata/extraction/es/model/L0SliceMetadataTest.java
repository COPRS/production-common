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

package esa.s1pdgs.cpoc.metadata.extraction.es.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class L0SliceMetadataTest {
	
	/**
	 * Test toString
	 */
	@Test
	public void testToString() {
		L0SliceMetadata obj = new L0SliceMetadata();
		obj.setProductName("name");
		obj.setMissionId("S1");
		obj.setSatelliteId("A");
		obj.setProductType("type");
		obj.setKeyObjectStorage("kobs");
		obj.setValidityStart("start");
		obj.setValidityStop("stop");
		obj.setInstrumentConfigurationId(15);
		obj.setNumberSlice(4);
		obj.setDatatakeId("14256");
		
		String str = obj.toJsonString();
		assertTrue(str.contains("productName\":\"name"));
		assertTrue(str.contains("missionId\":\"S1\""));
		assertTrue(str.contains("satelliteId\":\"A\""));
		assertTrue(str.contains("productType\":\"type"));
		assertTrue(str.contains("keyObjectStorage\":\"kobs"));
		assertTrue(str.contains("validityStart\":\"start"));
		assertTrue(str.contains("validityStop\":\"stop"));
		assertTrue(str.contains("instrumentConfigurationId\":15"));
		assertTrue(str.contains("numberSlice\":4"));
		assertTrue(str.contains("datatakeId\":\"14256"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0SliceMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
