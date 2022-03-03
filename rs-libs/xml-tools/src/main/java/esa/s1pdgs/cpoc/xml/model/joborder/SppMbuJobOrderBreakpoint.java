package esa.s1pdgs.cpoc.xml.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BreakPoint")
@XmlAccessorType(XmlAccessType.NONE)
public class SppMbuJobOrderBreakpoint extends AbstractJobOrderBreakpoint {

    public SppMbuJobOrderBreakpoint(AbstractJobOrderBreakpoint other) {
        super(other);
    }

    public SppMbuJobOrderBreakpoint() {
        super();
    }
}
