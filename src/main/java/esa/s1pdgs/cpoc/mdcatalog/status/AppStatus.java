package esa.s1pdgs.cpoc.mdcatalog.status;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.AppState;
import esa.s1pdgs.cpoc.common.ProductCategory;

/**
 * Application status
 * 
 * @author Viveris Technologies
 */
@Component
public class AppStatus {

    /**
     * Maximal number of consecutive errors for processing
     */
    private final int maxErrorCounterProcessing;
    
    /**
     * Maximal number of consecutive errors for MQI
     */
    private final int maxErrorCounterNextMessage;

	/**
	 * Identifier of the processing message
	 */
	private Map<ProductCategory, StatusPerCategory> status;

	/**
	 * Constructor
	 * 
	 * @param maxErrorCounter
	 */
	@Autowired
	public AppStatus(@Value("${status.max-error-counter-processing}") final int maxErrorCounterProcessing, 
	        @Value("${status.max-error-counter-mqi}") final int maxErrorCounterNextMessage) {
		this.maxErrorCounterProcessing = maxErrorCounterProcessing;
		this.maxErrorCounterNextMessage = maxErrorCounterNextMessage;
		this.status = new HashMap<>();
		this.status.put(ProductCategory.AUXILIARY_FILES, new StatusPerCategory(ProductCategory.AUXILIARY_FILES));
		this.status.put(ProductCategory.EDRS_SESSIONS, new StatusPerCategory(ProductCategory.EDRS_SESSIONS));
		this.status.put(ProductCategory.LEVEL_PRODUCTS, new StatusPerCategory(ProductCategory.LEVEL_PRODUCTS));
	}

	/**
	 * @return the status
	 */
	public synchronized Map<ProductCategory, StatusPerCategory> getStatus() {
		return status;
	}

	/**
	 * Get the global application status
	 * @return the status
	 */
	public AppState getGlobalAppState() {
		boolean isFatalError = false;
		boolean isError = false;
		boolean isProcessing = false;
		for (StatusPerCategory catStatus : status.values()) {
			if (catStatus.isFatalError()) {
				isFatalError = true;
			} else if (catStatus.isError()) {
				isError = true;
			} else if (catStatus.isProcessing()) {
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

	/**
	 * True if one category is in FATAL ERROR
	 * @return
	 */
	public boolean isFatalError() {
		return getGlobalAppState() == AppState.FATALERROR;
	}

	/**
	 * @return the processingMsgId
	 */
	public long getProcessingMsgId(final ProductCategory category) {
		long ret = 0;
		if (status.containsKey(category)) {
			ret = status.get(category).getProcessingMsgId();
		}
		return ret;
	}

	/**
	 * Set application as waiting
	 */
	public synchronized void setWaiting(final ProductCategory category) {
		if (status.containsKey(category)) {
			status.get(category).setWaiting();
		}
	}

	/**
	 * Set application as processing
	 */
	public synchronized void setProcessing(final ProductCategory category, final long processingMsgId) {
		if (status.containsKey(category)) {
			status.get(category).setProcessing(processingMsgId);
		}
	}

	/**
	 * Set application as error
	 */
	public synchronized void setError(final ProductCategory category, final String ErrorType) {
		if (status.containsKey(category)) {
		    if(ErrorType.equals("PROCESSING")) {
		        status.get(category).setErrorProcessing(maxErrorCounterProcessing);
		    }
		    if(ErrorType.equals("NEXT_MESSAGE")) {
		        status.get(category).setErrorNextMessage(maxErrorCounterNextMessage);
		    }
		}
	}
	
}
