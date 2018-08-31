package esa.s1pdgs.cpoc.jobgenerator.model.tasktable;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Object representing Dyn_ProcParam in task table
 * 
 * @author Cyrielle
 *
 */
@XmlRootElement(name = "Dyn_ProcParam")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableDynProcParam {

	/**
	 * 
	 */
	@XmlElement(name = "Param_Name")
	private String name;

	/**
	 * 
	 */
	@XmlElement(name = "Param_Type")
	private String type;

	/**
	 * 
	 */
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
	public TaskTableDynProcParam(final String name, final String type, final String defaultValue) {
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
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
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
	 * @return the defaultValue
	 */
	public String getDefaultValue() {
		return defaultValue;
	}

	/**
	 * @param defaultValue
	 *            the defaultValue to set
	 */
	public void setDefaultValue(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, type, defaultValue);
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
			TaskTableDynProcParam other = (TaskTableDynProcParam) obj;
			ret = Objects.equals(name, other.name) && Objects.equals(type, other.type)
					&& Objects.equals(defaultValue, other.defaultValue);
		}
		return ret;
	}

}
