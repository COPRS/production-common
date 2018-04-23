package fr.viveris.s1pdgs.jobgenerator.model.joborder.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

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

	private final String value;

	JobOrderFileNameType(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static JobOrderFileNameType fromValue(String v) {
		for (JobOrderFileNameType c : JobOrderFileNameType.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}
}
