package esa.s1pdgs.cpoc.xml.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlType(name = "Mode")
@XmlEnum
public enum TaskTableInputMode {

	@XmlEnumValue("ALWAYS")
	ALWAYS,

	@XmlEnumValue("SLICING")
	SLICING,

	@XmlEnumValue("NON_SLICING")
	NON_SLICING,
 
    @XmlEnumValue("")
    BLANK;
}
