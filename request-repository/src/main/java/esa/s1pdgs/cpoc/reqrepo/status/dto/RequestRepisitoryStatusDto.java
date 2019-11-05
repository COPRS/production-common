package esa.s1pdgs.cpoc.reqrepo.status.dto;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;

public class RequestRepisitoryStatusDto {
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
    
    public RequestRepisitoryStatusDto() {
        timeSinceLastChange = 0;
        errorCounter = 0;
    }

    public RequestRepisitoryStatusDto(final AppState state, final long timeLastChange,
            final int errorCounter) {
        super();
        this.status = state;
        this.timeSinceLastChange = timeLastChange;
        this.errorCounter = errorCounter;
    }

    public AppState getStatus() {
        return status;
    }

    public void setStatus(final AppState status) {
        this.status = status;
    }

    public long getTimeSinceLastChange() {
        return timeSinceLastChange;
    }

    public void setTimeSinceLastChange(final long timeLastChange) {
        this.timeSinceLastChange = timeLastChange;
    }

    public int getErrorCounter() {
        return errorCounter;
    }

    public void setErrorCounter(final int errorCounter) {
        this.errorCounter = errorCounter;
    }

    @Override
    public String toString() {
        return String.format(
                "{status: %s, timeSinceLastChange: %d, errorCounter: %d}",
                status, timeSinceLastChange, errorCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, timeSinceLastChange, errorCounter);
    }

    @Override
    public boolean equals(final Object obj) {
        boolean ret;
        if (this == obj) {
            ret = true;
        } else if (obj == null || getClass() != obj.getClass()) {
            ret = false;
        } else {
        	RequestRepisitoryStatusDto other = (RequestRepisitoryStatusDto) obj;
            // field comparison
            ret = Objects.equals(status, other.status)
                    && timeSinceLastChange == other.timeSinceLastChange
                    && errorCounter == other.errorCounter;
        }
        return ret;
    }

}
