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

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderOutput;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderDestination;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class JobOrderOutputTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		JobOrderOutput obj = new JobOrderOutput();
		obj.setDestination(JobOrderDestination.PROC);
		obj.setFamily(ProductFamily.AUXILIARY_FILE);
		obj.setFileName("file");
		obj.setFileNameType(JobOrderFileNameType.PHYSICAL);
		obj.setFileType("type");
		obj.setMandatory(true);

		JobOrderOutput clone = new JobOrderOutput(obj);
		assertEquals(obj.isMandatory(), clone.isMandatory());
		assertEquals(obj.getFileType(), clone.getFileType());
		assertEquals(obj.getFileNameType(), clone.getFileNameType());
		assertEquals(obj.getFileName(), clone.getFileName());
		assertEquals(obj.getFamily(), clone.getFamily());
		assertEquals(obj.getDestination(), clone.getDestination());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderOutput obj = new JobOrderOutput();
		obj.setDestination(JobOrderDestination.PROC);
		obj.setFamily(ProductFamily.AUXILIARY_FILE);
		obj.setFileName("file");
		obj.setFileNameType(JobOrderFileNameType.PHYSICAL);
		obj.setFileType("type");
		obj.setMandatory(true);

		String str = obj.toString();
		assertTrue(str.contains("mandatory: true"));
		assertTrue(str.contains("fileType: type"));
		assertTrue(str.contains("fileNameType: PHYSICAL"));
		assertTrue(str.contains("fileName: file"));
		assertTrue(str.contains("family: AUXILIARY_FILE"));
		assertTrue(str.contains("destination: PROC"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderOutput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
