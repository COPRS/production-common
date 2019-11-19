package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */
@XmlRootElement(name = "Task")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableTask {

	/**
	 * 
	 */
	@XmlElement(name = "Name")
	private String name;

	/**
	 * 
	 */
	@XmlElement(name = "Version")
	private String version;

	/**
	 * 
	 */
	@XmlElement(name = "Critical")
	private boolean critical;

	/**
	 * 
	 */
	@XmlElement(name = "Criticality_Level")
	private int criticityLevel;

	/**
	 * 
	 */
	@XmlElement(name = "File_Name")
	private String fileName;

	/**
	 * 
	 */
	@XmlElementWrapper(name = "List_of_Inputs")
	@XmlElement(name = "Input")
	private List<TaskTableInput> inputs;

	/**
	 * 
	 */
	@XmlElementWrapper(name = "List_of_Outputs")
	@XmlElement(name = "Output")
	private List<TaskTableOuput> outputs;

	/**
	 * 
	 */
	public TaskTableTask() {
		super();
		this.critical = false;
		this.criticityLevel = 1;
		this.inputs = new ArrayList<>();
		this.outputs = new ArrayList<>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(final String version) {
		this.version = version;
	}

	/**
	 * @return the critical
	 */
	public boolean isCritical() {
		return critical;
	}

	/**
	 * @param critical
	 *            the critical to set
	 */
	public void setCritical(final boolean critical) {
		this.critical = critical;
	}

	/**
	 * @return the criticityLevel
	 */
	public int getCriticityLevel() {
		return criticityLevel;
	}

	/**
	 * @param criticityLevel
	 *            the criticityLevel to set
	 */
	public void setCriticityLevel(final int criticityLevel) {
		this.criticityLevel = criticityLevel;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the inputs
	 */
	public List<TaskTableInput> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void setInputs(final List<TaskTableInput> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @param inputs
	 *            the inputs to set
	 */
	public void addInput(final TaskTableInput input) {
		this.inputs.add(input);
	}

	/**
	 * @return the outputs
	 */
	public List<TaskTableOuput> getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void setOutputs(final List<TaskTableOuput> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @param outputs
	 *            the outputs to set
	 */
	public void addOutput(final TaskTableOuput output) {
		this.outputs.add(output);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, version, critical, criticityLevel, fileName, inputs, outputs);
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
			TaskTableTask other = (TaskTableTask) obj;
			ret = Objects.equals(name, other.name) && Objects.equals(version, other.version)
					&& critical == other.critical && criticityLevel == other.criticityLevel
					&& Objects.equals(fileName, other.fileName) && Objects.equals(inputs, other.inputs)
					&& Objects.equals(outputs, other.outputs);
		}
		return ret;
	}
}
