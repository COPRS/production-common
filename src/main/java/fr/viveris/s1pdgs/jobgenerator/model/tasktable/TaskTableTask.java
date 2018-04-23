package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Task")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableTask {
	
	@XmlElement(name = "Name")
	private String name;
	
	@XmlElement(name = "Version")
	private String version;
	
	@XmlElement(name = "Critical")
	private boolean critical;
	
	@XmlElement(name = "Criticality_Level")
	private int criticityLevel;
	
	@XmlElement(name = "File_Name")
	private String fileName;

	@XmlElementWrapper(name = "List_of_Inputs")
	@XmlElement(name = "Input")
	private List<TaskTableInput> inputs;

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
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the critical
	 */
	public boolean isCritical() {
		return critical;
	}

	/**
	 * @param critical the critical to set
	 */
	public void setCritical(boolean critical) {
		this.critical = critical;
	}

	/**
	 * @return the criticityLevel
	 */
	public int getCriticityLevel() {
		return criticityLevel;
	}

	/**
	 * @param criticityLevel the criticityLevel to set
	 */
	public void setCriticityLevel(int criticityLevel) {
		this.criticityLevel = criticityLevel;
	}

	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the inputs
	 */
	public List<TaskTableInput> getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(List<TaskTableInput> inputs) {
		this.inputs = inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void addInput(TaskTableInput input) {
		this.inputs.add(input);
	}

	/**
	 * @return the outputs
	 */
	public List<TaskTableOuput> getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(List<TaskTableOuput> outputs) {
		this.outputs = outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void addOutput(TaskTableOuput output) {
		this.outputs.add(output);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTableTask [name=" + name + ", version=" + version + ", critical=" + critical + ", criticityLevel="
				+ criticityLevel + ", fileName=" + fileName + ", inputs=" + inputs + ", outputs=" + outputs + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (critical ? 1231 : 1237);
		result = prime * result + criticityLevel;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + ((inputs == null) ? 0 : inputs.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((outputs == null) ? 0 : outputs.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	/* (non-Javadoc)
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
		TaskTableTask other = (TaskTableTask) obj;
		if (critical != other.critical)
			return false;
		if (criticityLevel != other.criticityLevel)
			return false;
		if (fileName == null) {
			if (other.fileName != null)
				return false;
		} else if (!fileName.equals(other.fileName))
			return false;
		if (inputs == null) {
			if (other.inputs != null)
				return false;
		} else if (!inputs.equals(other.inputs))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (outputs == null) {
			if (other.outputs != null)
				return false;
		} else if (!outputs.equals(other.outputs))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
	
	

}
