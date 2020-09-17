package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class listing the breakpoint configuration in a job order
 *
 * @author Cyrielle Gailliard
 */
@XmlRootElement(name = "Breakpoint")
@XmlAccessorType(XmlAccessType.NONE)
public class StandardJobOrderBreakpoint extends AbstractJobOrderBreakpoint {

    /**
     * Breakpoint enable: OFF | ON
     */
    @XmlElement(name = "Enable")
    private String enable;

    public StandardJobOrderBreakpoint() {
        super();
        this.enable = "OFF";
    }

    public StandardJobOrderBreakpoint(String enable, List<String> files) {
        super(files);
        this.enable = enable;
    }

    public StandardJobOrderBreakpoint(AbstractJobOrderBreakpoint obj) {
        super(obj);
        if(obj instanceof StandardJobOrderBreakpoint) {
            enable = ((StandardJobOrderBreakpoint) obj).enable;
        } else {
            enable = "OFF";
        }
    }

    /**
     * @return the enable
     */
    public String getEnable() {
        return enable;
    }

    /**
     * @param enable the enable to set
     */
    public void setEnable(final String enable) {
        this.enable = enable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        StandardJobOrderBreakpoint that = (StandardJobOrderBreakpoint) o;
        return Objects.equals(enable, that.enable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enable);
    }
}
