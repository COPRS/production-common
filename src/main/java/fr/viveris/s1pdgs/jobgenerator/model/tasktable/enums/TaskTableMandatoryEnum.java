package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlType(name = "Mandatory")
@XmlEnum
public enum TaskTableMandatoryEnum {

	@XmlEnumValue("Yes")
	YES,

	@XmlEnumValue("No")
	NO,
 
    @XmlEnumValue("")
    BLANK;
}
