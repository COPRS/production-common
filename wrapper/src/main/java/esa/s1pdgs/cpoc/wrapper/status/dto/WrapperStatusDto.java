package esa.s1pdgs.cpoc.wrapper.status.dto;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;

/**
 * @author Viveris Technologies
 */
public class WrapperStatusDto {

    /**
     * Status
     */
    private AppState status;

    /**
     * Number of milliseconds passed for the last modification
     */
    private long timeSinceLastChange;

    /**
     * Number of error for the last modification of status
     */
    private int errorCounter;
    
    public WrapperStatusDto() {
        timeSinceLastChange = 0;
        errorCounter = 0;
    }

    /**
     * @param state
     * @param lastChange
     * @param errorCounter
     */
    public WrapperStatusDto(final AppState state, final long timeLastChange,
            final int errorCounter) {
        super();
        this.status = state;
        this.timeSinceLastChange = timeLastChange;
        this.errorCounter = errorCounter;
    }

    /**
     * @return the status
     */
    public AppState getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(final AppState status) {
        this.status = status;
    }

    /**
     * @return the timeSinceLastChange
     */
    public long getTimeSinceLastChange() {
        return timeSinceLastChange;
    }

    /**
     * @param timeLastChange
     *            the timeLastChange to set
     */
    public void setTimeSinceLastChange(final long timeLastChange) {
        this.timeSinceLastChange = timeLastChange;
    }

    /**
     * @return the errorCounter
     */
    public int getErrorCounter() {
        return errorCounter;
    }

    /**
     * @param errorCounter
     *            the errorCounter to set
     */
    public void setErrorCounter(final int errorCounter) {
        this.errorCounter = errorCounter;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{status: %s, timeSinceLastChange: %d, errorCounter: %d}",
                status, timeSinceLastChange, errorCounter);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, timeSinceLastChange, errorCounter);
    }

    /**
     * @see java.lang.Object#equals()
     */
    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
            WrapperStatusDto other = (WrapperStatusDto) obj;
            // field comparison
            ret = Objects.equals(status, other.status)
                    && timeSinceLastChange == other.timeSinceLastChange
                    && errorCounter == other.errorCounter;
        }
        return ret;
    }

}
