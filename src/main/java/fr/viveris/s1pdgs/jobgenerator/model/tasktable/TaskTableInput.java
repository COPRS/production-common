package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableInputMode;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;

@XmlRootElement(name = "Input")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableInput {
	
	@XmlAttribute(name = "id")
	private String id;
	
	@XmlAttribute(name = "ref")
	private String reference;
	
	@XmlElement(name = "Mode")
	private TaskTableInputMode mode;
	
	@XmlElement(name = "Mandatory")
	private TaskTableMandatoryEnum mandatory;

	@XmlElementWrapper(name = "List_of_Alternatives")
	@XmlElement(name = "Alternative")
	private List<TaskTableInputAlternative> alternatives;

	/**
	 * 
	 */
	public TaskTableInput() {
		super();
		this.mode = TaskTableInputMode.BLANK;
		this.mandatory = TaskTableMandatoryEnum.NO;
		this.alternatives = new ArrayList<>();
	}

	/**
	 * 
	 */
	public TaskTableInput(String reference) {
		super();
		this.reference = reference;
	}

	/**
	 * @param mode
	 * @param mandatory
	 */
	public TaskTableInput(TaskTableInputMode mode, TaskTableMandatoryEnum mandatory) {
		this();
		this.mode = mode;
		this.mandatory = mandatory;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @param reference the reference to set
	 */
	public void setReference(String reference) {
		this.reference = reference;
	}

	/**
	 * @return the mode
	 */
	public TaskTableInputMode getMode() {
		return mode;
	}

	/**
	 * @param mode the mode to set
	 */
	public void setMode(TaskTableInputMode mode) {
		this.mode = mode;
	}

	/**
	 * @return the mandatory
	 */
	public TaskTableMandatoryEnum getMandatory() {
		return mandatory;
	}

	/**
	 * @param mandatory the mandatory to set
	 */
	public void setMandatory(TaskTableMandatoryEnum mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @return the alternatives
	 */
	public List<TaskTableInputAlternative> getAlternatives() {
		return alternatives;
	}

	/**
	 * @param alternatives the alternatives to set
	 */
	public void setAlternatives(List<TaskTableInputAlternative> alternatives) {
		this.alternatives = alternatives;
	}

	/**
	 * @param alternatives the alternatives to set
	 */
	public void addAlternatives(List<TaskTableInputAlternative> alternatives) {
		this.alternatives.addAll(alternatives);
	}

	/**
	 * @param alternatives the alternatives to set
	 */
	public void addAlternative(TaskTableInputAlternative alternative) {
		this.alternatives.add(alternative);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTableInput [id=" + id + ", reference=" + reference + ", mode=" + mode + ", mandatory=" + mandatory
				+ ", alternatives=" + alternatives + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alternatives == null) ? 0 : alternatives.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mandatory == null) ? 0 : mandatory.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
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
		TaskTableInput other = (TaskTableInput) obj;
		if (alternatives == null) {
			if (other.alternatives != null)
				return false;
		} else if (!alternatives.equals(other.alternatives))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mandatory != other.mandatory)
			return false;
		if (mode != other.mode)
			return false;
		if (reference == null) {
			if (other.reference != null)
				return false;
		} else if (!reference.equals(other.reference))
			return false;
		return true;
	}

}
