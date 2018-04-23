package fr.viveris.s1pdgs.jobgenerator.model.tasktable;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Pool")
@XmlAccessorType(XmlAccessType.NONE)
public class TaskTablePool {
	
	@XmlElement(name = "Detached")
	private boolean detached = false;
	
	@XmlElement(name = "Killing_Signal")
	private int killingSignal = 9;

	@XmlElementWrapper(name = "List_of_Tasks")
	@XmlElement(name = "Task")
	private List<TaskTableTask> tasks;

	/**
	 * 
	 */
	public TaskTablePool() {
		super();
		this.tasks = new ArrayList<>();
	}

	/**
	 * @return the detached
	 */
	public boolean isDetached() {
		return detached;
	}

	/**
	 * @param detached the detached to set
	 */
	public void setDetached(boolean detached) {
		this.detached = detached;
	}

	/**
	 * @return the killingSignal
	 */
	public int getKillingSignal() {
		return killingSignal;
	}

	/**
	 * @param killingSignal the killingSignal to set
	 */
	public void setKillingSignal(int killingSignal) {
		this.killingSignal = killingSignal;
	}

	/**
	 * @return the tasks
	 */
	public List<TaskTableTask> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(List<TaskTableTask> tasks) {
		this.tasks = tasks;
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void addTask(TaskTableTask task) {
		this.tasks.add(task);
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void addTasks(List<TaskTableTask> tasks) {
		this.tasks.addAll(tasks);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TaskTablePool [detached=" + detached + ", killingSignal=" + killingSignal + ", tasks=" + tasks + "]";
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (detached ? 1231 : 1237);
		result = prime * result + killingSignal;
		result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
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
		TaskTablePool other = (TaskTablePool) obj;
		if (detached != other.detached)
			return false;
		if (killingSignal != other.killingSignal)
			return false;
		if (tasks == null) {
			if (other.tasks != null)
				return false;
		} else if (!tasks.equals(other.tasks))
			return false;
		return true;
	}
	
	
}
