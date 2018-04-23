package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import fr.viveris.s1pdgs.jobgenerator.model.tasktable.enums.TaskTableOutputDestination;

@XmlRootElement(name = "Output")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableOuput {
	
	@XmlElement(name = "Destination")
	private TaskTableOutputDestination destination;
	
	@XmlElement(name = "Mandatory")
	private TaskTableMandatoryEnum mandatory;
	
	@XmlElement(name = "Type")
	private String type;
	
	@XmlElement(name = "File_Name_Type")
	private TaskTableFileNameType fileNameType;

	/**
	 * 
	 */
	public TaskTableOuput() {
		super();
		this.destination = TaskTableOutputDestination.BLANK;
		this.mandatory = TaskTableMandatoryEnum.NO;
		this.fileNameType = TaskTableFileNameType.BLANK;
	}

	/**
	 * @param destination
	 * @param mandatory
	 * @param type
	 * @param fileNameType
	 */
	public TaskTableOuput(TaskTableOutputDestination destination, TaskTableMandatoryEnum mandatory, String type,
			TaskTableFileNameType fileNameType) {
		this();
		this.destination = destination;
		this.mandatory = mandatory;
		this.type = type;
		this.fileNameType = fileNameType;
	}

	/**
	 * @return the destination
	 */
	public TaskTableOutputDestination getDestination() {
		return destination;
	}

	/**
	 * @param destination the destination to set
	 */
	public void setDestination(TaskTableOutputDestination destination) {
		this.destination = destination;
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the fileNameType
	 */
	public TaskTableFileNameType getFileNameType() {
		return fileNameType;
	}

	/**
	 * @param fileNameType the fileNameType to set
	 */
	public void setFileNameType(TaskTableFileNameType fileNameType) {
		this.fileNameType = fileNameType;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTableOuput [destination=" + destination + ", mandatory=" + mandatory + ", type=" + type
				+ ", fileNameType=" + fileNameType + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((destination == null) ? 0 : destination.hashCode());
		result = prime * result + ((fileNameType == null) ? 0 : fileNameType.hashCode());
		result = prime * result + ((mandatory == null) ? 0 : mandatory.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		TaskTableOuput other = (TaskTableOuput) obj;
		if (destination == null) {
			if (other.destination != null)
				return false;
		} else if (!destination.equals(other.destination))
			return false;
		if (fileNameType == null) {
			if (other.fileNameType != null)
				return false;
		} else if (!fileNameType.equals(other.fileNameType))
			return false;
		if (!mandatory.equals(other.mandatory))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	

}
