package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 */
@XmlType(name = "Destination")
@XmlEnum
public enum TaskTableOutputDestination {

	@XmlEnumValue("DB")
	DB,

	@XmlEnumValue("PROC")
	PROC,
 
    @XmlEnumValue("")
    BLANK;
}
