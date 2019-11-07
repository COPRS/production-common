package esa.s1pdgs.cpoc.status;

import esa.s1pdgs.cpoc.common.AppState;

public class Status {

    /**
     * State
     */
    private AppState state;

    /**
     * Date of the last change of the status (old status != new status)
     */
    private long dateLastChangeMs;

    /**
     * Number of consecutive errors for processing
     */
    private int errorCounterProcessing;
    
    /**
     * Number of consecutive errors for next message
     */
    private int errorCounterNextMessage;

    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterProcessing;
    
    /**
     * Maximal number of consecutive errors for MQI
     */
    private final int maxErrorCounterNextMessage;
    
    public Status(int maxErrorCounterProcessing, int maxErrorCounterNextMessage) {
    	this.maxErrorCounterProcessing = maxErrorCounterProcessing;
    	this.maxErrorCounterNextMessage = maxErrorCounterNextMessage;
        state = AppState.WAITING;        
        errorCounterProcessing = 0;
        errorCounterNextMessage = 0;
        dateLastChangeMs = System.currentTimeMillis();
    }

    /**
     * @return the status
     */
    public AppState getState() {
        return state;
    }

    /**
     * @return the timeSinceLastChange
     */
    public long getDateLastChangeMs() {
        return dateLastChangeMs;
    }

    /**
     * @return the errorCounter
     */
    public int getErrorCounterProcessing() {
        return errorCounterProcessing;
    }

    /**
     * @return the errorCounterNextMessage
     */
    public int getErrorCounterNextMessage() {
        return errorCounterNextMessage;
    }

    /**
     * Set status WAITING
     */
    public void setWaiting() {
        if (!isStopping() && !isFatalError()) {
            state = AppState.WAITING;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterNextMessage = 0;
        }
    }

    /**
     * Set status PROCESSING
     */
    public void setProcessing() {
        if (!isStopping() && !isFatalError()) {
            state = AppState.PROCESSING;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterProcessing = 0;
            errorCounterNextMessage = 0;
        }
    }

    /**
     * Set status STOPPING
     */
    public void setStopping() {
        state = AppState.STOPPING;
        dateLastChangeMs = System.currentTimeMillis();
        errorCounterProcessing = 0;
        errorCounterNextMessage = 0;
    }

    public void incrementErrorCounterProcessing() {
        if (!isStopping()) {
            state = AppState.ERROR;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterProcessing++;
            if (errorCounterProcessing >= maxErrorCounterProcessing) {
                setFatalError();
            }
        }
    }
    
    public void incrementErrorCounterNextMessage() {
        if (!isStopping()) {
            state = AppState.ERROR;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterNextMessage++;
            if (errorCounterNextMessage >= maxErrorCounterNextMessage) {
                setFatalError();
            }
        }
    }

    /**
     * Set status FATALERROR
     */
    public void setFatalError() {
        state = AppState.FATALERROR;
        dateLastChangeMs = System.currentTimeMillis();
    }

    /**
     * Indicate if state is waiting
     * 
     * @return
     */
    public boolean isWaiting() {
        return state == AppState.WAITING;
    }

    /**
     * Indicate if state is PROCESSING
     * 
     * @return
     */
    public boolean isProcessing() {
        return state == AppState.PROCESSING;
    }

    /**
     * Indicate if state is STOPPING
     * 
     * @return
     */
    public boolean isStopping() {
        return state == AppState.STOPPING;
    }

    /**
     * Indicate if state is ERROR
     * 
     * @return
     */
    public boolean isError() {
        return state == AppState.ERROR;
    }

    /**
     * Indicate if state is FATALERROR
     * 
     * @return
     */
    public boolean isFatalError() {
        return state == AppState.FATALERROR;
    }

}