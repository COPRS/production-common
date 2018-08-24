package esa.s1pdgs.cpoc.mdcatalog.status;

import java.util.Objects;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * @author Viveris Technologies
 */
public class StatusPerCategory {

    /**
     * State
     */
    private AppState state;

    /**
     * Category
     */
    private final ProductCategory category;

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
     * Processing message identifier
     */
    private long processingMsgId;

    /**
     * Constructor
     */
    public StatusPerCategory(final ProductCategory category) {
        this.state = AppState.WAITING;
        this.errorCounterProcessing = 0;
        this.errorCounterNextMessage = 0;
        this.dateLastChangeMs = System.currentTimeMillis();
        this.category = category;
        this.processingMsgId = 0;
    }

    /**
     * @return the category
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * @return the processingMsgId
     */
    public long getProcessingMsgId() {
        return processingMsgId;
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
        this.processingMsgId = 0;
        if (!isFatalError()) {
            state = AppState.WAITING;
            dateLastChangeMs = System.currentTimeMillis();
        }
    }

    /**
     * Set status PROCESSING
     */
    public void setProcessing(final long processingMsgId) {
        if (!isFatalError()) {
            state = AppState.PROCESSING;
            this.processingMsgId = processingMsgId;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterProcessing = 0;
            errorCounterNextMessage = 0;
        }
    }

    /**
     * Set status ERROR
     */
    public void setErrorProcessing(final int maxErrorCounter) {
        if (!isFatalError()) {
            state = AppState.ERROR;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterProcessing++;
            if (errorCounterProcessing >= maxErrorCounter) {
                setFatalError();
            }
        }
    }
    
    /**
     * @param errorCounterNextMessage the errorCounterNextMessage to set
     */
    public void setErrorNextMessage(final int maxErrorCounterNextMessage) {
        if (!isFatalError()) {
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

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "{state: %s, category: %s, dateLastChangeMs: %s, errorCounterProcessing: %s, "
                + "errorCounterNextMessage: %s, processingMsgId: %s}",
                state, category, dateLastChangeMs, errorCounterProcessing,
                errorCounterNextMessage, processingMsgId);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(state, category, dateLastChangeMs, errorCounterProcessing,
                errorCounterNextMessage, processingMsgId);
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
            StatusPerCategory other = (StatusPerCategory) obj;
            // field comparison
            ret = Objects.equals(state, other.state)
                    && Objects.equals(category, other.category)
                    && dateLastChangeMs == other.dateLastChangeMs
                    && errorCounterProcessing == other.errorCounterProcessing
                    && errorCounterNextMessage == other.errorCounterNextMessage
                    && processingMsgId == other.processingMsgId;
        }
        return ret;
    }

}
