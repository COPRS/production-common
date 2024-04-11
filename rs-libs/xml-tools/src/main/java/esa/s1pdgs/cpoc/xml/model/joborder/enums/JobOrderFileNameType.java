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

package esa.s1pdgs.cpoc.xml.model.joborder.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Available file name type in the job Order
 * @author Cyrielle Gailliard
 *
 */
@XmlType(name = "File_Name_Type")
@XmlEnum
public enum JobOrderFileNameType {

	@XmlEnumValue("Physical")
	PHYSICAL("Physical"),

	@XmlEnumValue("Directory")
	DIRECTORY("Directory"),

	@XmlEnumValue("Regexp")
	REGEXP("Regexp"),

	@XmlEnumValue("")
	BLANK("");

	/**
	 * Value in XML file
	 */
	private final String value;

	/**
	 * 
	 * @param v
	 */
	JobOrderFileNameType(final String val) {
		value = val;
	}
	
	public String getValue() {
		return value;
	}
}
