package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "File_Name_Type")
@XmlEnum
public enum TaskTableFileNameType {

	@XmlEnumValue("Physical")
	PHYSICAL("Physical"),

	@XmlEnumValue("Directory")
	DIRECTORY("Directory"),

	@XmlEnumValue("Regexp")
	REGEXP("Regexp"),
 
    @XmlEnumValue("")
    BLANK("");
	
    private final String value;
    
    TaskTableFileNameType(String v) {
        value = v;
    }
 
    public String value() {
        return value;
    }
 
    public static TaskTableFileNameType fromValue(String v) {
        for (TaskTableFileNameType c: TaskTableFileNameType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
