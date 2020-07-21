package esa.s1pdgs.cpoc.appcatalog;

import java.util.List;
import java.util.Objects;

public class AppDataJobTaskInputs {

    private String taskName;
    private String taskVersion;
    private List<AppDataJobInput> inputs;

    public AppDataJobTaskInputs(String taskName, String taskVersion, List<AppDataJobInput> inputs) {
        this.taskName = taskName;
        this.taskVersion = taskVersion;
        this.inputs = inputs;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskVersion() {
        return taskVersion;
    }

    public void setTaskVersion(String taskVersion) {
        this.taskVersion = taskVersion;
    }

    public List<AppDataJobInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<AppDataJobInput> inputs) {
        this.inputs = inputs;
    }

    @Override
    public String toString() {
        return String.format("{taskName: %s, taskVersion: %s, inputs: %s}",
                taskName, taskVersion, inputs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppDataJobTaskInputs that = (AppDataJobTaskInputs) o;
        return Objects.equals(taskName, that.taskName) &&
                Objects.equals(taskVersion, that.taskVersion) &&
                Objects.equals(inputs, that.inputs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, taskVersion, inputs);
    }
}
