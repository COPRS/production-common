package esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.StringUtils;

import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableInputMode;
import esa.s1pdgs.cpoc.ipf.preparation.worker.model.tasktable.enums.TaskTableMandatoryEnum;

/**
 * 
 */
@XmlRootElement(name = "Input")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTableInput {

	/**
	 * 
	 */
	@XmlAttribute(name = "id")
	private String id;

	/**
	 * 
	 */
	@XmlAttribute(name = "ref")
	private String reference;

	/**
	 * 
	 */
	@XmlElement(name = "Mode")
	private TaskTableInputMode mode;

	/**
	 * 
	 */
	@XmlElement(name = "Mandatory")
	private TaskTableMandatoryEnum mandatory;

	/**
	 * 
	 */
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
	public TaskTableInput(final String reference) {
		super();
		this.reference = reference;
	}

	/**
	 */
	public TaskTableInput(final TaskTableInputMode mode, final TaskTableMandatoryEnum mandatory) {
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
	 * @param id
	 *            the id to set
	 */
	public void setId(final String id) {
		this.id = id;
	}

	/**
	 * @return the reference
	 */
	public String getReference() {
		return reference;
	}

	/**
	 * @return the mode
	 */
	public TaskTableInputMode getMode() {
		return mode;
	}

	/**
	 * @return the mandatory
	 */
	public TaskTableMandatoryEnum getMandatory() {
		return mandatory;
	}

	/**
	 * @return the alternatives
	 */
	public List<TaskTableInputAlternative> getAlternatives() {
		return alternatives;
	}

	//this has been taken from TaskTableAdapter to ensure
	//it is always used, but getAlternatives is still used in tests
	//also I'm not sure if this method is right in this place
	public Stream<TaskTableInputAlternative> alternativesOrdered() {
		if(alternatives == null) {
			return Stream.empty();
		}

		return alternatives.stream().sorted(TaskTableInputAlternative.ORDER);
	}

	/**
	 *
	 * For test purposes only
	 * @param alternative
	 *            the alternatives to set
	 */
	public void addAlternative(final TaskTableInputAlternative alternative) {
		this.alternatives.add(alternative);
	}
	
	/**
	 * 
	 */
	public String toLogMessage() {
		if (StringUtils.isEmpty(reference)) {
			return id;
		}
		return reference;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id, reference, mode, mandatory, alternatives);
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
			TaskTableInput other = (TaskTableInput) obj;
			ret = Objects.equals(id, other.id) && Objects.equals(reference, other.reference)
					&& Objects.equals(mode, other.mode) && Objects.equals(mandatory, other.mandatory)
					&& Objects.equals(alternatives, other.alternatives);
		}
		return ret;
	}

}
