package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlPath;

/**
 * Class describing a processor
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Ipf_Proc")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderProc {

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
	 * Breakpoints
	 */
	@XmlElement(name = "Breakpoint")
	private JobOrderBreakpoint breakpoint;

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
	public JobOrderProc() {
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
	public JobOrderProc(JobOrderProc obj) {
		this();
		this.inputs.addAll(obj.getInputs().stream().filter(item -> item != null).map(item -> new JobOrderInput(item))
				.collect(Collectors.toList()));
		this.nbInputs = this.inputs.size();
		this.outputs.addAll(obj.getOutputs().stream().filter(item -> item != null).map(item -> new JobOrderOutput(item))
				.collect(Collectors.toList()));
		this.nbOutputs = this.outputs.size();
		if (obj.getBreakpoint() != null) {
			this.breakpoint = new JobOrderBreakpoint(obj.getBreakpoint());
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
	 * @param taskName
	 *            the taskName to set
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * @return the taskVersion
	 */
	public String getTaskVersion() {
		return taskVersion;
	}

	/**
	 * @param taskVersion
	 *            the taskVersion to set
	 */
	public void setTaskVersion(String taskVersion) {
		this.taskVersion = taskVersion;
	}

	/**
	 * @return the breakpoint
	 */
	public JobOrderBreakpoint getBreakpoint() {
		return breakpoint;
	}

	/**
	 * @param breakpoint
	 *            the breakpoint to set
	 */
	public void setBreakpoint(JobOrderBreakpoint breakpoint) {
		this.breakpoint = breakpoint;
	}

	/**
	 * @return the inputs
	 */
	public List<JobOrderInput> getInputs() {
		return inputs;
	}

	public void addInput(JobOrderInput input) {
		this.inputs.add(input);
		this.nbInputs++;
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void addInputs(List<JobOrderInput> inputs) {
		if (inputs != null) {
			this.inputs.addAll(inputs);
			this.nbInputs += inputs.size();
		}
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void setInputs(List<JobOrderInput> inputs) {
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
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutput(JobOrderOutput output) {
		this.outputs.add(output);
		this.nbOutputs++;
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutputs(List<JobOrderOutput> outputs) {
		if (outputs != null) {
			this.outputs.addAll(outputs);
			this.nbOutputs += outputs.size();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderProc [taskName=" + taskName + ", taskVersion=" + taskVersion + ", breakpoint=" + breakpoint
				+ ", inputs=" + inputs + ", nbInputs=" + nbInputs + ", outputs=" + outputs + ", nbOutputs=" + nbOutputs
				+ "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((breakpoint == null) ? 0 : breakpoint.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + nbInputs;
		result = prime * result + nbOutputs;
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + ((taskVersion == null) ? 0 : taskVersion.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JobOrderProc other = (JobOrderProc) obj;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		if (taskVersion == null) {
			if (other.taskVersion != null)
				return false;
		} else if (!taskVersion.equals(other.taskVersion))
			return false;
		if (breakpoint == null) {
			if (other.breakpoint != null)
				return false;
		} else if (!breakpoint.equals(other.breakpoint))
			return false;
		if (nbInputs != other.nbInputs)
			return false;
		if (nbOutputs != other.nbOutputs)
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (outputs == null) {
			if (other.outputs != null)
				return false;
		} else if (!outputs.equals(other.outputs))
			return false;
		return true;
	}

}
