package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.common.ApplicationLevel;

/**
 * Class describing a processor
 *
 * @author Cyrielle Gailliard
 */
@XmlRootElement(name = "Ipf_Proc")
@XmlAccessorType(XmlAccessType.NONE)
public class StandardJobOrderProc extends AbstractJobOrderProc {

    /**
     * Breakpoints
     */
    @XmlElement(name = "Breakpoint")
    private AbstractJobOrderBreakpoint breakpoint;


    public StandardJobOrderProc() {
        super();
    }

    public StandardJobOrderProc(AbstractJobOrderProc other, ApplicationLevel applicationLevel) {
        super(other, applicationLevel);
    }

    @Override
    public AbstractJobOrderBreakpoint getBreakpoint() {
        return breakpoint;
    }

    @Override
    public void setBreakpoint(AbstractJobOrderBreakpoint breakpoint) {
        this.breakpoint = breakpoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardJobOrderProc that = (StandardJobOrderProc) o;
        return Objects.equals(breakpoint, that.breakpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), breakpoint);
    }
}
