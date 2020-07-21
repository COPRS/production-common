package esa.s1pdgs.cpoc.xml.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlType(name = "Origin")
@XmlEnum
public enum TaskTableInputOrigin {

	@XmlEnumValue("PROC")
	PROC,

	@XmlEnumValue("DB")
	DB,
 
    @XmlEnumValue("")
    BLANK;
}
