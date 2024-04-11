/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
