package esa.s1pdgs.cpoc.mdcatalog.status;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.status.AbstractAppStatus;
import esa.s1pdgs.cpoc.status.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    @Autowired
    public AppStatusImpl(@Value("${status.max-error-counter-processing}") final int maxErrorCounterProcessing, 
	        @Value("${status.max-error-counter-mqi}") final int maxErrorCounterNextMessage) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage));
    	addSubStatus(new Status(ProductCategory.AUXILIARY_FILES, maxErrorCounterProcessing, maxErrorCounterNextMessage));
    	addSubStatus(new Status(ProductCategory.EDRS_SESSIONS, maxErrorCounterProcessing, maxErrorCounterNextMessage));
    	addSubStatus(new Status(ProductCategory.LEVEL_PRODUCTS, maxErrorCounterProcessing, maxErrorCounterNextMessage));
    }
    
//    /**
//	 * @return the status
//	 */
//	public synchronized Map<ProductCategory, Status> getSubStatus() {
//		return null;
//	}
//	
//	public AppState getGlobalAppState() {
//		boolean isFatalError = false;
//		boolean isError = false;
//		boolean isProcessing = false;
//		for (Status subStatus : getSubStatuses().values()) {
//			if (subStatus.isFatalError()) {
//				isFatalError = true;
//			} else if (subStatus.isError()) {
//				isError = true;
//			} else if (subStatus.isProcessing()) {
//				isProcessing = true;
//			}
//		}
//		AppState ret = AppState.WAITING;
//		if (isFatalError) {
//			ret = AppState.FATALERROR;
//		} else if (isError) {
//			ret = AppState.ERROR;
//		} else if (isProcessing) {
//			ret = AppState.PROCESSING;
//		}
//		return ret;
//	}

	/**
	 * @return the processingMsgId
	 */
	public long getProcessingMsgId(final ProductCategory category) {
		long ret = 0;
		if (getSubStatuses().containsKey(category)) {
			ret = getSubStatuses().get(category).getProcessingMsgId();
		}
		return ret;
	}

	/**
	 * Set application as waiting
	 */
	public synchronized void setWaiting(final ProductCategory category) {
		if (getSubStatuses().containsKey(category)) {
			getSubStatuses().get(category).setWaiting();
		}
	}

	/**
	 * Set application as processing
	 */
	public synchronized void setProcessing(final ProductCategory category, final long processingMsgId) {
		if (getSubStatuses().containsKey(category)) {
			getSubStatuses().get(category).setProcessing(processingMsgId);
		}
	}

	/**
	 * Set application as error
	 */
	public synchronized void setError(final ProductCategory category, final String ErrorType) {
		if (getSubStatuses().containsKey(category)) {
		    if(ErrorType.equals("PROCESSING")) {
		    	getSubStatuses().get(category).incrementErrorCounterProcessing();
		    }
		    if(ErrorType.equals("NEXT_MESSAGE")) {
		    	getSubStatuses().get(category).incrementErrorCounterNextMessage();
		    }
		}
	}
	
    @Override
    public boolean isProcessing(String category, long messageId) {
    	if (!ProductCategory.EDRS_SESSIONS.name().toLowerCase().equals(category) &&
                !ProductCategory.LEVEL_PRODUCTS.name().toLowerCase().equals(category)) {
    		throw new NoSuchElementException(String.format("Category %s not available for processing", category));
    	} else if (messageId < 0) {
    		throw new IllegalArgumentException(String.format("Message id value %d is out of range", messageId));			
    	}		
    	return getProcessingMsgId() != Status.PROCESSING_MSG_ID_UNDEFINED && getProcessingMsgId() == messageId;
    }

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms:3000}")
    public void forceStopping() {
        if (isShallBeStopped()) {
            System.exit(0);
        }
    }

}
