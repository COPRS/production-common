package esa.s1pdgs.cpoc.appstatus;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;

public class Status {
	public static final Status NULL = new Status(0, 0);

	/**
	 * For waiting state and computations where no MQI is involved
	 */
	public static final long PROCESSING_MSG_ID_UNDEFINED = 0;
	
    /**
     * State of application
     */
    private AppState state;

    /**
     * Category of state
     */
    private Optional<ProductCategory> category;
    
    /**
     * Status per category
     */
    private Map<ProductCategory, Status> subStatuses;
    
    /**
     * Identifier of the processing message
     */
    private long processingMsgId;
    
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
    
    public Status(final ProductCategory productCategory, final int maxErrorCounterProcessing, final int maxErrorCounterNextMessage) {
    	this(maxErrorCounterProcessing, maxErrorCounterNextMessage);
    	category = Optional.ofNullable(productCategory);
    }
    
    public Status(final int maxErrorCounterProcessing, final int maxErrorCounterNextMessage) {
    	this.maxErrorCounterProcessing = maxErrorCounterProcessing;
    	this.maxErrorCounterNextMessage = maxErrorCounterNextMessage;
    	processingMsgId = PROCESSING_MSG_ID_UNDEFINED;
    	state = AppState.WAITING;        
    	errorCounterProcessing = 0;
    	errorCounterNextMessage = 0;
    	dateLastChangeMs = System.currentTimeMillis();
    	category = Optional.empty();
    	subStatuses = new HashMap<>();
    }
    
	/**
	 * @return the category
	 */
	public Optional<ProductCategory> getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(final Optional<ProductCategory> category) {
		this.category = category;
	}

    /**
     * @return the status
     */
    public AppState getState() {
    	if (subStatuses.isEmpty() || state == AppState.STOPPING) {
    		return state;
    	} else {
    		// note: this code block has been moved from metadata catalog
    		boolean isFatalError = false;
    		boolean isError = false;
    		boolean isProcessing = false;
    		for(final Status subStatus : subStatuses.values()) {
    			if (subStatus.isFatalError()) {
    				isFatalError = true;
    			} else if (subStatus.isError()) {
    				isError = true;
    			} else if (subStatus.isProcessing()) {
    				isProcessing = true;
    			}
    		}
    		AppState ret = AppState.WAITING;
    		if (isFatalError) {
    			ret = AppState.FATALERROR;
    		} else if (isError) {
    			ret = AppState.ERROR;
    		} else if (isProcessing) {
    			ret = AppState.PROCESSING;
    		}
    		return ret;
    	}
    }

    /**
	 * @return the processingMsgId
	 */
	public long getProcessingMsgId() {
		return processingMsgId;
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
    	if (subStatuses.isEmpty()) { 
    		return errorCounterProcessing;
    	} else {
    		int sum = 0;
			for(final Status subStatus : subStatuses.values()) {
				sum +=  subStatus.getErrorCounterProcessing();
			}
			return sum;
    	}
    }

    /**
     * @return the errorCounterNextMessage
     */
    public int getErrorCounterNextMessage() {
    	if (subStatuses.isEmpty()) { 
    		return errorCounterNextMessage;
    	} else {
    		int sum = 0;
			for(final Status subStatus : subStatuses.values()) {
				sum += subStatus.getErrorCounterNextMessage();						
			}
			return sum;
    	}
    }

    /**
     * Set status WAITING
     */
    public void setWaiting() {
        if (!isStopping() && !isFatalError()) {
            state = AppState.WAITING;
            processingMsgId = PROCESSING_MSG_ID_UNDEFINED;
            dateLastChangeMs = System.currentTimeMillis();
            errorCounterNextMessage = 0;
        }
    }

    /**
     * Set status PROCESSING
     */
    public void setProcessing(final long processingMsgId) {
        if (!isStopping() && !isFatalError()) {
        	this.processingMsgId = processingMsgId;
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
   		return getState() == AppState.WAITING;
    }

    /**
     * Indicate if state is PROCESSING
     * 
     * @return
     */
    public boolean isProcessing() {
    	return getState() == AppState.PROCESSING;
    }

    /**
     * Indicate if state is STOPPING
     * 
     * @return
     */
    public boolean isStopping() {
        return state == AppState.STOPPING; // for stopping direct access to state is required
    }

    /**
     * Indicate if state is ERROR
     * 
     * @return
     */
    public boolean isError() {
   		return getState() == AppState.ERROR;
    }

    /**
     * Indicate if state is FATALERROR
     * 
     * @return
     */
    public boolean isFatalError() {
   		return getState() == AppState.FATALERROR;
    }

	/**
	 * @return the subStatuses
	 */
	public Map<ProductCategory, Status> getSubStatuses() {
		return subStatuses;
	}

	/**
	 * @param subStatuses the subStatuses to set
	 */
	public void setSubStatuses(final Map<ProductCategory, Status> subStatuses) {
		this.subStatuses = subStatuses;
	}

}