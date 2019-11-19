package esa.s1pdgs.cpoc.ipf.preparation.worker.model.joborder.enums;

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
