package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlType(name = "Test")
@XmlEnum
public enum TaskTableTestEnum {

	@XmlEnumValue("Yes")
	YES,

	@XmlEnumValue("No")
	NO,
 
    @XmlEnumValue("")
    BLANK;
}
