package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO object representing a pool of a job (a set of execution tasks to be
 * processing in parallel)
 * 
 * @author Viveris Technologies
 * @see JobDto
 */
public class JobPoolDto {

    /**
     * List of tasks
     */
    private List<JobTaskDto> tasks;

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
    public void setTasks(final List<JobTaskDto> tasks) {
        this.tasks = tasks;
    }

    /**
     * @param tasks
     *            the tasks to set
     */
    public void addTask(final JobTaskDto task) {
        this.tasks.add(task);
    }

    /**
     * To string
     */
    @Override
    public String toString() {
        return String.format("{tasks: %s}", tasks);
    }

    /**
     * Hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(tasks);
    }

    /**
     * Equals
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            JobPoolDto other = (JobPoolDto) obj;
            ret = Objects.equals(tasks, other.tasks);
        }
        return ret;
    }

}
