package esa.s1pdgs.cpoc.jobgenerator.model.l2routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Class used the to route<br/>
 * Used for mapping the file routing.xml in java objects
 * 
 * @author Cyrielle Gailliard
 *
 */
@XmlRootElement(name = "l2_to")
@XmlAccessorType(XmlAccessType.NONE)
public class L2RouteTo {

	/**
	 * List of task tables (XML filenames)
	 */
	@XmlElementWrapper(name = "task_tables")
	@XmlElement(name = "task_table")
	private List<String> taskTables;

	/**
	 * Default constructor
	 */
	public L2RouteTo() {
		this.taskTables = new ArrayList<>();
	}

	/**
	 * Constructor using fields
	 * @param taskTables
	 */
	public L2RouteTo(final Collection<String> taskTables) {
		this();
		this.taskTables.addAll(taskTables);
	}

	/**
	 * @return the taskTables
	 */
	public List<String> getTaskTables() {
		return taskTables;
	}

	/**
	 * @param taskTables the taskTables to set
	 */
	public void setTaskTables(final List<String> taskTables) {
		this.taskTables = taskTables;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("{taskTables: %s}", taskTables);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(taskTables);
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
			L2RouteTo other = (L2RouteTo) obj;
			ret = Objects.equals(taskTables, other.taskTables);
		}
		return ret;
	}

}
