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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class L0SliceMetadataDtoTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		L0SliceMetadata obj = new L0SliceMetadata("name", "type", "kobs", "startDate", "stopDate", "mission", "satellite", "station");
		obj.setInstrumentConfigurationId(1);
		obj.setDatatakeId("dataTakeId");
		obj.setNumberSlice(8);
		
		String str = obj.toJsonString();
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"validityStart\":\"startDate\""));
		assertTrue(str.contains("\"validityStop\":\"stopDate\""));
		assertTrue(str.contains("\"missionId\":\"mission\""));
		assertTrue(str.contains("\"satelliteId\":\"satellite\""));
		assertTrue(str.contains("\"stationCode\":\"station\""));
		assertTrue(str.contains("\"instrumentConfigurationId\":1"));
		assertTrue(str.contains("\"datatakeId\":\"dataTakeId\""));
		assertTrue(str.contains("\"numberSlice\":8"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(L0SliceMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
