package esa.s1pdgs.cpoc.appcatalog;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

public class AppDataJobInput {

    private String fileType;
    private String fileNameType;

    private List<AppDataJobFile> files;

    public AppDataJobInput() {
    }

    public AppDataJobInput(String fileType, String fileNameType, List<AppDataJobFile> files) {
        this.fileType = fileType;
        this.fileNameType = fileNameType;
        this.files = files;
    }

    public AppDataJobInput(AppDataJobInput other) {
        if(other.files != null) {
            files = other.files.stream().map(AppDataJobFile::new).collect(toList());
        }
        fileType = other.fileType;
        fileNameType = other.fileNameType;
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
    public String toString() {
        return String.format("{fileType: %s, fileNameType: %s, files: %s}", fileType, fileNameType, files);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppDataJobInput that = (AppDataJobInput) o;
        return Objects.equals(fileType, that.fileType) &&
                Objects.equals(fileNameType, that.fileNameType) &&
                Objects.equals(files, that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileType, fileNameType, files);
    }
}
