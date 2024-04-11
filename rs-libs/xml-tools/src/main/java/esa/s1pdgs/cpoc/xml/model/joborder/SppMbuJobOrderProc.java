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
