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

package esa.s1pdgs.cpoc.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.FileExtension;

/**
 * Test enumeration FileExtension
 */
public class FileExtensionTest {
	
	/**
	 * Test when extension is valid
	 */
	@Test
	public void testValidExtensions() {
		assertEquals(FileExtension.XML, FileExtension.valueOfIgnoreCase("xml"));
		assertEquals(FileExtension.DAT, FileExtension.valueOfIgnoreCase("DAT"));
		assertEquals(FileExtension.XSD, FileExtension.valueOfIgnoreCase("xsd"));
		assertEquals(FileExtension.EOF, FileExtension.valueOfIgnoreCase("EoF"));
		assertEquals(FileExtension.SAFE, FileExtension.valueOfIgnoreCase("Safe"));
	}
	
	/**
	 * Test when extension is invalid
	 */
	@Test
	public void testInvalidExtensions() {
		assertEquals(FileExtension.UNKNOWN, FileExtension.valueOfIgnoreCase("xmld"));
		assertEquals(FileExtension.UNKNOWN, FileExtension.valueOfIgnoreCase("UNKNOWN"));
	}
}
