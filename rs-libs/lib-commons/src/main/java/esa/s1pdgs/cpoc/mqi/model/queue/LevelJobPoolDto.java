/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Viveris Technologies
 * @see IpfExecutionJob
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
