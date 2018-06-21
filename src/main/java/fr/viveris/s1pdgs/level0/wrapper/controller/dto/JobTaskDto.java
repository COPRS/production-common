package fr.viveris.s1pdgs.level0.wrapper.controller.dto;

import java.util.Objects;

/**
 * DTO object representing a task to be executed for the job
 * 
 * @author Viveris Technologies
 * @see JobDto
 */
public class JobTaskDto {

    /**
     * Absolute path of the binary
     */
    private String binaryPath;

    /**
     * Default constructor
     */
    public JobTaskDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param binaryPath
     */
    public JobTaskDto(final String binaryPath) {
        this();
        this.binaryPath = binaryPath;
    }

    /**
     * @return the binaryPath
     */
    public String getBinaryPath() {
        return binaryPath;
    }

    /**
     * @param binaryPath
     *            the binaryPath to set
     */
    public void setBinaryPath(final String binaryPath) {
        this.binaryPath = binaryPath;
    }

    /**
     * to string
     */
    @Override
    public String toString() {
        return String.format("{binaryPath: %s}", binaryPath);
    }

    /**
     * hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(binaryPath);
    }

    /**
     * equals
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            JobTaskDto other = (JobTaskDto) obj;
            ret = Objects.equals(binaryPath, other.binaryPath);
        }
        return ret;
    }

}
