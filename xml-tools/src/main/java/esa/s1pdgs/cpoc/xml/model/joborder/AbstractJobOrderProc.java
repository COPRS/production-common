package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import esa.s1pdgs.cpoc.common.ApplicationLevel;

@XmlRootElement(name = "Ipf_Proc")
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractJobOrderProc {

    /**
     * Task name
     */
    @XmlElement(name = "Task_Name")
    private String taskName;

    /**
     * Task version
     */
    @XmlElement(name = "Task_Version")
    private String taskVersion;

    /**
     * Inputs
     */
    @XmlElementWrapper(name = "List_of_Inputs")
    @XmlElement(name = "Input")
    private List<JobOrderInput> inputs;

    /**
     * Number of inputs. Automatically filled.
     */
    @XmlPath("List_of_Inputs/@count")
    private int nbInputs;

    /**
     * Outputs
     */
    @XmlElementWrapper(name = "List_of_Outputs")
    @XmlElement(name = "Output")
    private List<JobOrderOutput> outputs;

    /**
     * Number of outputs. Automatically filled.
     */
    @XmlPath("List_of_Outputs/@count")
    private int nbOutputs;

    /**
     * Default constructor
     */
    public AbstractJobOrderProc() {
        super();
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.nbInputs = 0;
        this.nbOutputs = 0;
    }

    /**
     * Clone
     *
     * @param obj
     */
    public AbstractJobOrderProc(final AbstractJobOrderProc obj, ApplicationLevel applicationLevel) {
        this();
        this.inputs.addAll(obj.getInputs().stream().filter(Objects::nonNull).map(JobOrderInput::new)
                .collect(Collectors.toList()));
        this.nbInputs = this.inputs.size();
        this.outputs.addAll(obj.getOutputs().stream().filter(Objects::nonNull).map(JobOrderOutput::new)
                .collect(Collectors.toList()));
        this.nbOutputs = this.outputs.size();
        if (obj.getBreakpoint() != null) {
        	switch (applicationLevel) {
        		case SPP_MBU: setBreakpoint(new SppMbuJobOrderBreakpoint(obj.getBreakpoint())); break;
        		case SPP_OBS: setBreakpoint(new SppObsJobOrderBreakpoint(obj.getBreakpoint())); break;
        		default: setBreakpoint(new StandardJobOrderBreakpoint(obj.getBreakpoint()));
        	}
        }
        this.taskName = obj.getTaskName();
        this.taskVersion = obj.getTaskVersion();
    }

    /**
     * @return the taskName
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * @param taskName the taskName to set
     */
    public void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    /**
     * @return the taskVersion
     */
    public String getTaskVersion() {
        return taskVersion;
    }

    /**
     * @param taskVersion the taskVersion to set
     */
    public void setTaskVersion(final String taskVersion) {
        this.taskVersion = taskVersion;
    }

    /**
     * @return the breakpoint
     */
    public abstract AbstractJobOrderBreakpoint getBreakpoint();

    /**
     * @param breakpoint the breakpoint to set
     */
    public abstract void setBreakpoint(final AbstractJobOrderBreakpoint breakpoint);

    /**
     * @return the inputs
     */
    public List<JobOrderInput> getInputs() {
        return inputs;
    }

    public void addInput(final JobOrderInput input) {
        this.inputs.add(input);
        this.nbInputs++;
    }

    public void updateInput(final int index, final JobOrderInput input) {
        if (index >= 0 && index < this.inputs.size()) {
            this.inputs.set(index, input);
        }
    }

    /**
     * @param inputs the inputs to set
     */
    public void setInputs(final List<JobOrderInput> inputs) {
        if (inputs != null) {
            this.inputs = inputs;
            this.nbInputs = inputs.size();
        } else {
            this.inputs = new ArrayList<>();
            this.nbInputs = 0;
        }
    }

    /**
     * @return the outputs
     */
    public List<JobOrderOutput> getOutputs() {
        return outputs;
    }

    /**
     * @param output the output to set
     */
    public void addOutput(final JobOrderOutput output) {
        this.outputs.add(output);
        this.nbOutputs++;
    }

    /**
     * @param outputs the outputs to set
     */
    public void addOutputs(final List<JobOrderOutput> outputs) {
        if (outputs != null) {
            this.outputs.addAll(outputs);
            this.nbOutputs += outputs.size();
        }
    }

    /**
     * @param outputs the outputs to set
     */
    public void setOutputs(final List<JobOrderOutput> outputs) {
        if (outputs != null) {
            this.outputs = outputs;
            this.nbOutputs = outputs.size();
        } else {
            this.outputs = new ArrayList<>();
            this.nbOutputs = 0;
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{taskName: %s, taskVersion: %s, breakpoint: %s, inputs: %s, nbInputs: %s, outputs: %s, nbOutputs: %s}",
                taskName, taskVersion, getBreakpoint(), inputs, nbInputs, outputs, nbOutputs);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(taskName, taskVersion, inputs, nbInputs, outputs, nbOutputs);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            AbstractJobOrderProc other = (AbstractJobOrderProc) obj;
            ret = Objects.equals(taskName, other.taskName) && Objects.equals(taskVersion, other.taskVersion)
                    && Objects.equals(inputs, other.inputs)
                    && nbInputs == other.nbInputs && Objects.equals(outputs, other.outputs)
                    && nbOutputs == other.nbOutputs;
        }
        return ret;
    }

}
