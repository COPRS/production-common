package esa.s1pdgs.cpoc.appcatalog;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class AppDataJobPreselectedInput {

    private String taskTableInputReference;
    private String fileType;
    private String fileNameType;
    private List<AppDataJobFile> files;

    public AppDataJobPreselectedInput() {
        taskTableInputReference= "";
        files = Collections.emptyList();
    }

    public AppDataJobPreselectedInput(final String taskTableInputReference,final String fileType,final String fileNameType,final List<AppDataJobFile> files) {
        this.taskTableInputReference = taskTableInputReference;
        this.fileType = fileType;
        this.fileNameType = fileNameType;
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

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getFileNameType() {
        return fileNameType;
    }

    public void setFileNameType(String fileNameType) {
        this.fileNameType = fileNameType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppDataJobPreselectedInput that = (AppDataJobPreselectedInput) o;
        return Objects.equals(taskTableInputReference, that.taskTableInputReference)
                && Objects.equals(fileType, that.fileType)
                && Objects.equals(fileNameType, that.fileNameType)
                && Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskTableInputReference, fileType, fileNameType, files);
    }
}
