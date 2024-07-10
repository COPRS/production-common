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

package esa.s1pdgs.cpoc.metadata.extraction.service.extraction.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Test the object EdrsSessionFile
 * 
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionFileTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {
		EdrsSessionFile file = new EdrsSessionFile();
		assertNull(file.getSessionId());
		assertNull(file.getStartTime());
		assertNull(file.getStopTime());
		assertNotNull(file.getRawNames());
		assertTrue(file.getRawNames().size() == 0);

		EdrsSessionFileRaw raw1 = new EdrsSessionFileRaw("raw1");
		EdrsSessionFileRaw raw2 = new EdrsSessionFileRaw("raw2");
		EdrsSessionFileRaw raw3 = new EdrsSessionFileRaw("raw3");
		file = new EdrsSessionFile("session-id", "2014-12-04T14:59:20Z", "2014-12-04T15:22:20Z", Arrays.asList(raw1, raw2, raw3));
		assertEquals("session-id", file.getSessionId());
		assertEquals("2014-12-04T14:59:20Z", file.getStartTime());
		assertEquals("2014-12-04T15:22:20Z", file.getStopTime());
		assertNotNull(file.getRawNames());
		assertTrue(file.getRawNames().size() == 3);
		assertEquals(raw2, file.getRawNames().get(1));
	}
	
	/**
	 * Test to string
	 */
	@Test
	public void testToString() {
		EdrsSessionFileRaw raw1 = new EdrsSessionFileRaw("raw1");
		EdrsSessionFileRaw raw2 = new EdrsSessionFileRaw("raw2");
		EdrsSessionFileRaw raw3 = new EdrsSessionFileRaw("raw3");
		
		EdrsSessionFile file = new EdrsSessionFile();
		file.setSessionId("session-id");
		file.setStartTime("2014-12-04T14:59:20Z");
		file.setStopTime("2014-12-04T15:22:20Z");
		file.setRawNames(Arrays.asList(raw1, raw2, raw3));
		
		String str = file.toString();
		assertTrue(str.contains("sessionId: session-id"));
		assertTrue(str.contains("startTime: 2014-12-04T14:59:20Z"));
		assertTrue(str.contains("stopTime: 2014-12-04T15:22:20Z"));
		assertTrue(str.contains("rawNames: "));
		assertTrue(str.contains(raw3.toString()));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(EdrsSessionFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}
}
