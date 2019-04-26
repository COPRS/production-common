package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Viveris Technologies
 * @see LevelJobDto
 */
public class LevelJobPoolDto {

    /**
     * List of tasks
     */
    private List<LevelJobTaskDto> tasks;

    /**
     * Default constructor
     */
    public LevelJobPoolDto() {
        this.tasks = new ArrayList<>();
    }

    /**
     * @return the tasks
     */
    public List<LevelJobTaskDto> getTasks() {
        return tasks;
    }

    /**
     * @param tasks
     *            the tasks to set
     */
    public void setTasks(final List<LevelJobTaskDto> tasks) {
        this.tasks = tasks;
    }

    /**
     * @param tasks
     *            the tasks to set
     */
    public void addTask(final LevelJobTaskDto task) {
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
            LevelJobPoolDto other = (LevelJobPoolDto) obj;
            ret = Objects.equals(tasks, other.tasks);
        }
        return ret;
    }

}
