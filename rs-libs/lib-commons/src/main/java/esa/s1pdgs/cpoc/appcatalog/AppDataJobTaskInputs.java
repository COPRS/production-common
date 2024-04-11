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

package esa.s1pdgs.cpoc.appcatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AppDataJobTaskInputs {

    private String taskName = "NOT_DEFINDED";
    private String taskVersion = "NOT_DEFINDED";
    private List<AppDataJobInput> inputs = new ArrayList<>();;
   
    public AppDataJobTaskInputs() {

	}

	public AppDataJobTaskInputs(final String taskName, final String taskVersion, final List<AppDataJobInput> inputs) {
        this.taskName = taskName;
        this.taskVersion = taskVersion;
        this.inputs = inputs;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(final String taskName) {
        this.taskName = taskName;
    }

    public String getTaskVersion() {
        return taskVersion;
    }

    public void setTaskVersion(final String taskVersion) {
        this.taskVersion = taskVersion;
    }

    public List<AppDataJobInput> getInputs() {
        return inputs;
    }

    public void setInputs(final List<AppDataJobInput> inputs) {
        this.inputs = inputs;
    }

    @Override
    public String toString() {
        return String.format("{taskName: %s, taskVersion: %s, inputs: %s}",
                taskName, taskVersion, inputs);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AppDataJobTaskInputs that = (AppDataJobTaskInputs) o;
        return Objects.equals(taskName, that.taskName) &&
                Objects.equals(taskVersion, that.taskVersion) &&
                Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, taskVersion, inputs);
    }
}
