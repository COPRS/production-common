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

package esa.s1pdgs.cpoc.xml.model.tasktable.joborder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 *
 */
public class JobOrderTimeIntervalTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructorClone() {

		JobOrderTimeInterval obj = new JobOrderTimeInterval();
		obj.setStart("starttime");
		obj.setStop("stoptime");
		obj.setFileName("file");

		JobOrderTimeInterval clone = new JobOrderTimeInterval(obj);
		assertEquals(obj.getStart(), clone.getStart());
		assertEquals(obj.getStop(), clone.getStop());
		assertEquals(obj.getFileName(), clone.getFileName());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderTimeInterval obj = new JobOrderTimeInterval();
		obj.setStart("starttime");
		obj.setStop("stoptime");
		obj.setFileName("file");

		String str = obj.toString();
		assertTrue(str.contains("start: starttime"));
		assertTrue(str.contains("stop: stoptime"));
		assertTrue(str.contains("fileName: file"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderTimeInterval.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
