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

import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderInputFileTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		JobOrderInputFile obj = new JobOrderInputFile();
		obj.setFilename("file");
		obj.setKeyObjectStorage("kobs");

		JobOrderInputFile clone = new JobOrderInputFile(obj);
		assertEquals(obj.getKeyObjectStorage(), clone.getKeyObjectStorage());
		assertEquals(obj.getFilename(), clone.getFilename());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderInputFile obj = new JobOrderInputFile();
		obj.setFilename("file");
		obj.setKeyObjectStorage("kobs");

		String str = obj.toString();
		assertTrue(str.contains("filename: file"));
		assertTrue(str.contains("keyObjectStorage: kobs"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderInputFile.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
