package fr.viveris.s1pdgs.jobgenerator.controller.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO object representing a pool of a job (a set of execution tasks to be
 * processing in parallel)
 * 
 * @author Cyrielle Gailliard
 * @see JobDto
 */
public class JobPoolDto {

	/**
	 * List of tasks
	 */
	List<JobTaskDto> tasks;

	/**
	 * Default constructor
	 */
	public JobPoolDto() {
		this.tasks = new ArrayList<>();
	}

	/**
	 * @return the tasks
	 */
	public List<JobTaskDto> getTasks() {
		return tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void setTasks(List<JobTaskDto> tasks) {
		this.tasks = tasks;
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void addTask(JobTaskDto task) {
		this.tasks.add(task);
	}

	/**
	 * @param tasks
	 *            the tasks to set
	 */
	public void addTasks(List<JobTaskDto> tasks) {
		this.tasks.addAll(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "JobPoolDto [tasks=" + tasks + "]";
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
		result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
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
		JobPoolDto other = (JobPoolDto) obj;
		if (tasks == null) {
			if (other.tasks != null)
				return false;
		} else if (!tasks.equals(other.tasks))
			return false;
		return true;
	}

}
