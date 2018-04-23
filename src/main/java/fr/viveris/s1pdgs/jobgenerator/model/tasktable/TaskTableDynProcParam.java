package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object representing Dyn_ProcParam in task table
 * @author Cyrielle
 *
 */
@XmlRootElement(name = "Dyn_ProcParam")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableDynProcParam {
	
	@XmlElement(name = "Param_Name")
	private String name;
	
	@XmlElement(name = "Param_Type")
	private String type;
	
	@XmlElement(name = "Param_Default")
	private String defaultValue;

	/**
	 * 
	 */
	public TaskTableDynProcParam() {
		super();
	}

	/**
	 * @param name
	 * @param type
	 * @param defaultValue
	 */
	public TaskTableDynProcParam(String name, String type, String defaultValue) {
		this();
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
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
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue the defaultValue to set
	 */
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTableDynProcParam [name=" + name + ", type=" + type + ", defaultValue=" + defaultValue + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		TaskTableDynProcParam other = (TaskTableDynProcParam) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
}
