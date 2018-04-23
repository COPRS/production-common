package fr.viveris.s1pdgs.jobgenerator.model.l1routing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "l1_to")
@XmlAccessorType(XmlAccessType.NONE)
public class L1RouteTo {

	@XmlElementWrapper(name = "task_tables")
	@XmlElement(name = "task_table")
	private List<String> taskTables;

	public L1RouteTo() {
		this.taskTables = new ArrayList<>();
	}

	public L1RouteTo(Collection<String> taskTables) {
		this();
		this.taskTables.addAll(taskTables);
	}

	/**
	 * @return the taskTables
	 */
	public List<String> getTaskTables() {
		return taskTables;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "L1RouteTo [taskTables=" + taskTables + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taskTables == null) ? 0 : taskTables.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
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
		L1RouteTo other = (L1RouteTo) obj;
		if (taskTables == null) {
			if (other.taskTables != null)
				return false;
		} else if (!taskTables.equals(other.taskTables))
			return false;
		return true;
	}

}
