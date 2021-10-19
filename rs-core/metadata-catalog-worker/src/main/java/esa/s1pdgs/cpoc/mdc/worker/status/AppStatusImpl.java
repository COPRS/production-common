package esa.s1pdgs.cpoc.mdc.worker.status;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.ProductCategory;

@Component
public class AppStatusImpl extends AbstractAppStatus {
    @Autowired
    public AppStatusImpl(
    		@Value("${status.max-error-counter-processing}") final int maxErrorCounterProcessing, 
	        @Value("${status.max-error-counter-mqi}") final int maxErrorCounterNextMessage,
            @Qualifier("systemExitCall") final Runnable systemExitCall
	) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage), systemExitCall);
    	addSubStatus(new Status(ProductCategory.AUXILIARY_FILES, maxErrorCounterProcessing, maxErrorCounterNextMessage));
    	addSubStatus(new Status(ProductCategory.EDRS_SESSIONS, maxErrorCounterProcessing, maxErrorCounterNextMessage));
    	addSubStatus(new Status(ProductCategory.LEVEL_PRODUCTS, maxErrorCounterProcessing, maxErrorCounterNextMessage));
    }
    
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
    public boolean isProcessing(final String category, final long messageId) {
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
            systemExit();
        }
    }

}
