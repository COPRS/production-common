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

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderSensingTime;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 *
 */
public class JobOrderSensingTimeTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructorClone() {

		JobOrderSensingTime obj = new JobOrderSensingTime();
		obj.setStart("starttime");
		obj.setStop("stoptime");

		JobOrderSensingTime clone = new JobOrderSensingTime(obj);
		assertEquals(obj.getStart(), clone.getStart());
		assertEquals(obj.getStop(), clone.getStop());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderSensingTime obj = new JobOrderSensingTime();
		obj.setStart("starttime");
		obj.setStop("stoptime");

		String str = obj.toString();
		assertTrue(str.contains("start: starttime"));
		assertTrue(str.contains("stop: stoptime"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderSensingTime.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
