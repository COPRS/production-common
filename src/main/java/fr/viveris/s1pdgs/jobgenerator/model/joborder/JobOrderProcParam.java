package fr.viveris.s1pdgs.jobgenerator.model.joborder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class of a dynamic parameter in the job order
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "Dynamic_Processing_Parameter")
@XmlAccessorType(XmlAccessType.NONE)
public class JobOrderProcParam {
	
	/**
	 * Parameter name
	 */
	@XmlElement(name = "Name")
	private String name;
	
	/**
	 * Parameter value
	 */
	@XmlElement(name = "Value")
	private String value;

	/**
	 * Default constructor
	 */
	public JobOrderProcParam() {
		super();
	}

	/**
	 * Constructor using all fields
	 * @param name
	 * @param value
	 */
	public JobOrderProcParam(String name, String value) {
		this();
		this.name = name;
		this.value = value;
	}

	/**
	 * Clone
	 * @param name
	 * @param value
	 */
	public JobOrderProcParam(JobOrderProcParam obj) {
		this(obj.getName(), obj.getValue());
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobOrderProcParam [name=" + name + ", value=" + value + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		JobOrderProcParam other = (JobOrderProcParam) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
}
