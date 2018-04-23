package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Destination")
@XmlEnum
public enum TaskTableOutputDestination {

	@XmlEnumValue("DB")
	DB("DB"),

	@XmlEnumValue("PROC")
	PROC("PROC"),
 
    @XmlEnumValue("")
    BLANK("");
	
    private final String value;
    
    TaskTableOutputDestination(String v) {
        value = v;
    }
 
    public String value() {
        return value;
    }
 
    public static TaskTableOutputDestination fromValue(String v) {
        for (TaskTableOutputDestination c: TaskTableOutputDestination.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
