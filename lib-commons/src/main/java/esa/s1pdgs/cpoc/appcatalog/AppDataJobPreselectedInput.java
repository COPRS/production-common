package esa.s1pdgs.cpoc.appcatalog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppDataJobPreselectedInput {

    private String taskTableInputReference;
    private List<AppDataJobFile> files;

    public AppDataJobPreselectedInput() {
        taskTableInputReference= "";
        files = Collections.emptyList();
    }

    public AppDataJobPreselectedInput(String taskTableInputReference, List<AppDataJobFile> files) {
        this.taskTableInputReference = taskTableInputReference;
        this.files = files;
    }

    public String getTaskTableInputReference() {
        return taskTableInputReference;
    }

    public void setTaskTableInputReference(String taskTableInputReference) {
        this.taskTableInputReference = taskTableInputReference;
    }

    public List<AppDataJobFile> getFiles() {
        return files;
    }

    public void setFiles(List<AppDataJobFile> files) {
        this.files = files;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppDataJobPreselectedInput that = (AppDataJobPreselectedInput) o;
        return Objects.equals(taskTableInputReference, that.taskTableInputReference) && Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskTableInputReference, files);
    }
}
