package esa.s1pdgs.cpoc.xml.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "BreakPoint")
@XmlAccessorType(XmlAccessType.NONE)
public class SppObsJobOrderBreakpoint extends AbstractJobOrderBreakpoint {

    public SppObsJobOrderBreakpoint(AbstractJobOrderBreakpoint other) {
        super(other);
    }

    public SppObsJobOrderBreakpoint() {
        super();
    }
}
