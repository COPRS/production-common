package esa.s1pdgs.cpoc.mqi.model.queue;

import java.util.Objects;

/**
 * @author Viveris Technologies
 * @see LevelJobDto
 */
public class LevelJobTaskDto {

    /**
     * Absolute path of the binary
     */
    private String binaryPath;

    /**
     * Default constructor
     */
    public LevelJobTaskDto() {
        super();
    }

    /**
     * Constructor using fields
     * 
     * @param binaryPath
     */
    public LevelJobTaskDto(final String binaryPath) {
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
            LevelJobTaskDto other = (LevelJobTaskDto) obj;
            ret = Objects.equals(binaryPath, other.binaryPath);
        }
        return ret;
    }

}
