package fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "Mode")
@XmlEnum
public enum TaskTableInputMode {

	@XmlEnumValue("ALWAYS")
	ALWAYS("ALWAYS"),

	@XmlEnumValue("SLICING")
	SLICING("SLICING"),

	@XmlEnumValue("NON_SLICING")
	NON_SLICING("NON_SLICING"),
 
    @XmlEnumValue("")
    BLANK("");
	
    private final String value;
    
    TaskTableInputMode(String v) {
        value = v;
    }
 
    public String value() {
        return value;
    }
 
    public static TaskTableInputMode fromValue(String v) {
        for (TaskTableInputMode c: TaskTableInputMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
