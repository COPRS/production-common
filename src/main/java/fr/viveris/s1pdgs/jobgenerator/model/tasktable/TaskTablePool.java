package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 */
@XmlRootElement(name = "Pool")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTablePool {

	/**
	 * 
	 */
	@XmlElement(name = "Detached")
	private boolean detached;

	/**
	 * 
	 */
	@XmlElement(name = "Killing_Signal")
	private int killingSignal;

	/**
	 * 
	 */
	@XmlElementWrapper(name = "List_of_Tasks")
	@XmlElement(name = "Task")
	private List<TaskTableTask> tasks;

	/**
	 * 
	 */
	public TaskTablePool() {
		super();
		detached = false;
		killingSignal = 9;
		this.tasks = new ArrayList<>();
	}

	/**
	 * @return the detached
	 */
	public boolean isDetached() {
		return detached;
	}

	/**
	 * @param detached
	 *            the detached to set
	 */
	public void setDetached(final boolean detached) {
		this.detached = detached;
	}

	/**
	 * @return the killingSignal
	 */
	public int getKillingSignal() {
		return killingSignal;
	}

	/**
	 * @param killingSignal
	 *            the killingSignal to set
	 */
	public void setKillingSignal(final int killingSignal) {
		this.killingSignal = killingSignal;
	}

	/**
	 * @return the tasks
	 */
	public List<TaskTableTask> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void setTasks(final List<TaskTableTask> tasks) {
		this.tasks = tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void addTask(final TaskTableTask task) {
		this.tasks.add(task);
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void addTasks(final List<TaskTableTask> tasks) {
		this.tasks.addAll(tasks);
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return Objects.hash(detached, killingSignal, tasks);
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
			TaskTablePool other = (TaskTablePool) obj;
			ret = detached == other.detached && killingSignal == other.killingSignal
					&& Objects.equals(tasks, other.tasks);
		}
		return ret;
	}
}
