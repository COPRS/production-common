package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Origin")
@XmlEnum
public enum TaskTableInputOrigin {

	@XmlEnumValue("PROC")
	PROC("PROC"),

	@XmlEnumValue("DB")
	DB("DB"),
 
    @XmlEnumValue("")
    BLANK("");
	
    private final String value;
    
    TaskTableInputOrigin(String v) {
        value = v;
    }
 
    public String value() {
        return value;
    }
 
    public static TaskTableInputOrigin fromValue(String v) {
        for (TaskTableInputOrigin c: TaskTableInputOrigin.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
