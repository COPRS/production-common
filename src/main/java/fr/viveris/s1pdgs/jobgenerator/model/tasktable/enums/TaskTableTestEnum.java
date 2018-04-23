package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Test")
@XmlEnum
public enum TaskTableTestEnum {

	@XmlEnumValue("Yes")
	YES("Yes"),

	@XmlEnumValue("No")
	NO("No"),
 
    @XmlEnumValue("")
    BLANK("");
	
    private final String value;
    
    TaskTableTestEnum(String v) {
        value = v;
    }
 
    public String value() {
        return value;
    }
 
    public static TaskTableTestEnum fromValue(String v) {
        for (TaskTableTestEnum c: TaskTableTestEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
