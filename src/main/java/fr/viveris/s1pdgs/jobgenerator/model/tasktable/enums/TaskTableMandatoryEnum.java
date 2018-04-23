package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Mandatory")
@XmlEnum
public enum TaskTableMandatoryEnum {

	@XmlEnumValue("Yes")
	YES("Yes"),

	@XmlEnumValue("No")
	NO("No"),
 
    @XmlEnumValue("")
    BLANK("");
	
    private final String value;
    
    TaskTableMandatoryEnum(String v) {
        value = v;
    }
 
    public String value() {
        return value;
    }
 
    public static TaskTableMandatoryEnum fromValue(String v) {
        for (TaskTableMandatoryEnum c: TaskTableMandatoryEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
