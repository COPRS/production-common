package esa.s1pdgs.cpoc.jobgenerator.model.tasktable;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableFileNameType;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableMandatoryEnum;
import esa.s1pdgs.cpoc.jobgenerator.model.tasktable.enums.TaskTableOutputDestination;

/**
 * 
 */
@XmlRootElement(name = "Output")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableOuput {

	/**
	 * 
	 */
	@XmlElement(name = "Destination")
	private TaskTableOutputDestination destination;

	/**
	 * 
	 */
	@XmlElement(name = "Mandatory")
	private TaskTableMandatoryEnum mandatory;

	/**
	 * 
	 */
	@XmlElement(name = "Type")
	private String type;

	/**
	 * 
	 */
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
	public TaskTableOuput(final TaskTableOutputDestination destination, final TaskTableMandatoryEnum mandatory,
			final String type, final TaskTableFileNameType fileNameType) {
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
	 * @param destination
	 *            the destination to set
	 */
	public void setDestination(final TaskTableOutputDestination destination) {
		this.destination = destination;
	}

	/**
	 * @return the mandatory
	 */
	public TaskTableMandatoryEnum getMandatory() {
		return mandatory;
	}

	/**
	 * @param mandatory
	 *            the mandatory to set
	 */
	public void setMandatory(final TaskTableMandatoryEnum mandatory) {
		this.mandatory = mandatory;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}

	/**
	 * @return the fileNameType
	 */
	public TaskTableFileNameType getFileNameType() {
		return fileNameType;
	}

	/**
	 * @param fileNameType
	 *            the fileNameType to set
	 */
	public void setFileNameType(final TaskTableFileNameType fileNameType) {
		this.fileNameType = fileNameType;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(destination, mandatory, type, fileNameType);
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
			TaskTableOuput other = (TaskTableOuput) obj;
			ret = Objects.equals(destination, other.destination) && Objects.equals(mandatory, other.mandatory)
					&& Objects.equals(type, other.type) && Objects.equals(fileNameType, other.fileNameType);
		}
		return ret;
	}
}
