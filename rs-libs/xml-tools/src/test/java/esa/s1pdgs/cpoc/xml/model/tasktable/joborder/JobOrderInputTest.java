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
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInput;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderInputFile;
import esa.s1pdgs.cpoc.xml.model.joborder.JobOrderTimeInterval;
import esa.s1pdgs.cpoc.xml.model.joborder.enums.JobOrderFileNameType;
import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * 
 */
public class JobOrderInputTest {

	/**
	 * Test constructors
	 */
	@Test
	public void testConstructors() {

		JobOrderTimeInterval input2 = new JobOrderTimeInterval("start", "stop", "fileName");

		JobOrderInput obj = new JobOrderInput();
		obj.setFileType("name");
		obj.setFileNameType(JobOrderFileNameType.REGEXP);
		obj.addTimeInterval(input2);
		obj.addFilename("filename", "keyObjectStorage");
		obj.setFamily(ProductFamily.L0_ACN);

		JobOrderInput clone = new JobOrderInput(obj);
		assertEquals(obj.getFileType(), clone.getFileType());
		assertEquals(obj.getFileNameType(), clone.getFileNameType());
		assertEquals(obj.getFamily(), clone.getFamily());
		assertEquals(obj.getNbFilenames(), clone.getNbFilenames());
		assertEquals(obj.getNbTimeIntervals(), clone.getNbTimeIntervals());
		assertEquals(obj.getTimeIntervals().get(0), clone.getTimeIntervals().get(0));
		assertEquals(obj.getFilenames().get(0), clone.getFilenames().get(0));

		obj = new JobOrderInput();
		obj.setFileType("name");
		obj.setFileNameType(JobOrderFileNameType.REGEXP);
		obj.setFamily(ProductFamily.L0_ACN);

		JobOrderInput clone2 = new JobOrderInput(obj);
		assertEquals(obj.getFileType(), clone2.getFileType());
		assertEquals(obj.getFileNameType(), clone2.getFileNameType());
		assertEquals(obj.getFamily(), clone.getFamily());
		assertEquals(0, clone2.getNbFilenames());
		assertEquals(0, clone2.getNbTimeIntervals());
	}

	/**
	 * Test to string
	 */
	@Test
	public void testToString() {

		JobOrderInputFile input1 = new JobOrderInputFile("filename", "keyObjectStorage");
		JobOrderTimeInterval input2 = new JobOrderTimeInterval("start", "stop", "fileName");

		JobOrderInput obj = new JobOrderInput();
		obj.setFileType("name");
		obj.setFileNameType(JobOrderFileNameType.REGEXP);
		obj.addTimeInterval(input2);
		obj.addFilename("filename", "keyObjectStorage");
		obj.setFamily(ProductFamily.L0_ACN);

		String str = obj.toString();
		assertTrue(str.contains("fileType: name"));
		assertTrue(str.contains("fileNameType: REGEXP"));
		assertTrue(str.contains("filenames: "));
		assertTrue(str.contains(input1.toString()));
		assertTrue(str.contains("nbFilenames: 1"));
		assertTrue(str.contains("timeIntervals: "));
		assertTrue(str.contains(input2.toString()));
		assertTrue(str.contains("nbTimeIntervals: 1"));
		assertTrue(str.contains("family: L0_ACN"));
	}

	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(JobOrderInput.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}

}
