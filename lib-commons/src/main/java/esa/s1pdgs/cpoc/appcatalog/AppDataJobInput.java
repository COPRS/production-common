package esa.s1pdgs.cpoc.appcatalog;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

public class AppDataJobInput {

    private String taskTableInputReference;
    private String fileType;
    private String fileNameType;
    private boolean mandatory;
    private boolean hasResults;

    private List<AppDataJobFile> files;

    public AppDataJobInput() {
    }

    public AppDataJobInput(final String taskTableInputReference, final String fileType, final String fileNameType, final boolean mandatory, final List<AppDataJobFile> files) {
        this.taskTableInputReference = taskTableInputReference;
        this.fileType = fileType;
        this.fileNameType = fileNameType;
        this.mandatory = mandatory;
        this.files = files;
        this.hasResults = !files.isEmpty();
    }

    public AppDataJobInput(final String newTaskTableInputReference, final AppDataJobInput other) {
        if(other.files != null) {
            files = other.files.stream().map(AppDataJobFile::new).collect(toList());
        }
        taskTableInputReference = newTaskTableInputReference;
        fileType = other.fileType;
        fileNameType = other.fileNameType;
        mandatory = other.mandatory;
        hasResults = other.hasResults;
    }

    public String getTaskTableInputReference() {
        return taskTableInputReference;
    }

    public void setTaskTableInputReference(final String taskTableInputReference) {
        this.taskTableInputReference = taskTableInputReference;
    }

    public List<AppDataJobFile> getFiles() {
        return files;
    }

    public void setFiles(final List<AppDataJobFile> files) {
        this.files = files;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    public String getFileNameType() {
        return fileNameType;
    }

    public void setFileNameType(final String fileNameType) {
        this.fileNameType = fileNameType;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public void setMandatory(final boolean mandatory) {
        this.mandatory = mandatory;
    }

    public boolean getHasResults() {
        return hasResults;
    }

    public void setHasResults(final boolean hasResults) {
        this.hasResults = hasResults;
    }

    @Override
    public String toString() {
        return String.format(
        		"{inputReference: %s, fileType: %s, fileNameType: %s, mandatory: %s, files: %s, hasResult: %s}",
                taskTableInputReference, fileType, fileNameType, mandatory, files, hasResults);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final AppDataJobInput that = (AppDataJobInput) o;
        return Objects.equals(taskTableInputReference, that.taskTableInputReference) &&
                Objects.equals(fileType, that.fileType) &&
                Objects.equals(fileNameType, that.fileNameType) &&
                Objects.equals(mandatory, that.mandatory) &&
                Objects.equals(hasResults, that.hasResults) &&
                Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskTableInputReference, fileType, fileNameType, mandatory, hasResults, files);
    }
}
