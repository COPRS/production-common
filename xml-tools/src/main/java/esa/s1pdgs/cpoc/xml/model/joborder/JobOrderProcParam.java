package esa.s1pdgs.cpoc.xml.model.joborder;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Class of a dynamic parameter in the job order
 * 
 * @author Cyrielle Gailliard
 *
 */
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
	 * 
	 * @param name
	 * @param value
	 */
	public JobOrderProcParam(final String name, final String value) {
		this();
		this.name = name;
		this.value = value;
	}

	/**
	 * Clone
	 * 
	 * @param name
	 * @param value
	 */
	public JobOrderProcParam(final JobOrderProcParam obj) {
		this(obj.getName(), obj.getValue());
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{name: %s, value: %s}", name, value);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(name, value);
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
			JobOrderProcParam other = (JobOrderProcParam) obj;
			ret = Objects.equals(name, other.name) && Objects.equals(value, other.value);
		}
		return ret;
	}

}
