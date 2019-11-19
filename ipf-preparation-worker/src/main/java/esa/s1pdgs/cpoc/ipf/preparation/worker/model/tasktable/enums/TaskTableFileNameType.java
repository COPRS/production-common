package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlType(name = "File_Name_Type")
@XmlEnum
public enum TaskTableFileNameType {

	@XmlEnumValue("Physical")
	PHYSICAL,

	@XmlEnumValue("Directory")
	DIRECTORY,

	@XmlEnumValue("Regexp")
	REGEXP,
 
    @XmlEnumValue("")
    BLANK;
}
