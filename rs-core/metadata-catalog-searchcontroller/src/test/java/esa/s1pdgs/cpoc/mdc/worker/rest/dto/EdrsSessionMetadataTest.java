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

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import esa.s1pdgs.cpoc.metadata.model.EdrsSessionMetadata;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class EdrsSessionMetadataTest {

	/**
	 * Test toString
	 */
	//String productName, String productType, String keyObjectStorage, String validityStart,String validityStop
	@Test
	public void testToString() {
		EdrsSessionMetadata obj = new EdrsSessionMetadata("name", "type", "kobs", "session", "start", "stop", "vstart", "vstop", "mission", "satellite", "station", Arrays.<String>asList("a","b","c"));
		
		String str = obj.toJsonString(); System.out.println(str);
		assertTrue(str.contains("\"productName\":\"name\""));
		assertTrue(str.contains("\"productType\":\"type\""));
		assertTrue(str.contains("\"keyObjectStorage\":\"kobs\""));
		assertTrue(str.contains("\"startTime\":\"start\""));
		assertTrue(str.contains("\"stopTime\":\"stop\""));
		assertTrue(str.contains("\"validityStart\":\"vstart\""));
		assertTrue(str.contains("\"validityStop\":\"vstop\""));
		assertTrue(str.contains("\"missionId\":\"mission\""));
		assertTrue(str.contains("\"satelliteId\":\"satellite\""));
		assertTrue(str.contains("\"stationCode\":\"station\""));
		assertTrue(str.contains("\"rawNames\":[\"a\",\"b\",\"c\"]"));
	}

	/**
	 * Check equals and hashcode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionMetadata.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
