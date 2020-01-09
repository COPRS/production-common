package esa.s1pdgs.cpoc.execution.worker.job.process;

import java.util.Objects;

/**
 * @author Viveris Technologies
 */
public class TaskResult {

    /**
     * Binary
     */
    private String binary;

    /**
     * Exit code of the binary execution
     */
    private int exitCode;

    /**
     * @param binary
     * @param exitCode
     */
    public TaskResult(final String binary, final int exitCode) {
        super();
        this.binary = binary;
        this.exitCode = exitCode;
    }

    /**
     * @return the binary
     */
    public String getBinary() {
        return binary;
    }

    /**
     * @param binary
     *            the binary to set
     */
    public void setBinary(final String binary) {
        this.binary = binary;
    }

    /**
     * @return the exitCode
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * @param exitCode
     *            the exitCode to set
     */
    public void setExitCode(final int exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("{binary: %s, exitCode: %s}", binary, exitCode);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(binary, exitCode);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            TaskResult other = (TaskResult) obj;
            ret = Objects.equals(binary, other.binary)
                    && Objects.equals(exitCode, other.exitCode);
        }
        return ret;
    }

}
