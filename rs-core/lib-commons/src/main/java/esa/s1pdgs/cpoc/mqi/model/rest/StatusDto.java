package esa.s1pdgs.cpoc.mqi.model.rest;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;

/**
 * @author Viveris Technologies
 */
public class StatusDto {

    /**
     * Status
     */
    private AppState status;

    /**
     * Number of milliseconds passed for the last modification
     */
    private long msLastChange;

    /**
     * Number of error for the last modification of status
     */
    private int errorCounter;

    /**
     * Constrcutor
     */
    public StatusDto() {
        msLastChange = 0;
        errorCounter = 0;
    }

    /**
     * @param state
     * @param lastChange
     * @param errorCounter
     */
    public StatusDto(final AppState state, final long msLastChange,
            final int errorCounter) {
        super();
        this.status = state;
        this.msLastChange = msLastChange;
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
     * @return the msLastChange
     */
    public long getMsLastChange() {
        return msLastChange;
    }

    /**
     * @param msLastChange
     *            the msLastChange to set
     */
    public void setMsLastChange(final long msLastChange) {
        this.msLastChange = msLastChange;
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
        return String.format("{status: %s, msLastChange: %d, errorCounter: %d}",
                status, msLastChange, errorCounter);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(status, msLastChange, errorCounter);
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
            StatusDto other = (StatusDto) obj;
            // field comparison
            ret = Objects.equals(status, other.status)
                    && msLastChange == other.msLastChange
                    && errorCounter == other.errorCounter;
        }
        return ret;
    }

}
