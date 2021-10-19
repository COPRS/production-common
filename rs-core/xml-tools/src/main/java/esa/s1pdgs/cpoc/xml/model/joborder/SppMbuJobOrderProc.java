package esa.s1pdgs.cpoc.xml.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.common.ApplicationLevel;

@XmlRootElement(name = "Ipf_Proc")
@XmlAccessorType(XmlAccessType.NONE)
public class SppMbuJobOrderProc extends AbstractJobOrderProc {

    public SppMbuJobOrderProc() {
        super();
    }

    public SppMbuJobOrderProc(AbstractJobOrderProc other) {
        super(other, ApplicationLevel.SPP_MBU);
    }

    /**
     * Breakpoints
     */
    @XmlElement(name = "BreakPoint")
    private AbstractJobOrderBreakpoint breakpoint;


    @Override
    public AbstractJobOrderBreakpoint getBreakpoint() {
        return breakpoint;
    }

    @Override
    public void setBreakpoint(AbstractJobOrderBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }
}
