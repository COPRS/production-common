package esa.s1pdgs.cpoc.compression.worker.status;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.DefaultAppStatusImpl;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.ProductCategory;

@Component
public class AppStatusImpl extends DefaultAppStatusImpl {	
    @Autowired
    public AppStatusImpl(
            @Value("${status.max-error-counter-processing:100}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi:100}") final int maxErrorCounterNextMessage) {
    	super(maxErrorCounterProcessing, maxErrorCounterNextMessage);
    }

    @Override
    public boolean isProcessing(final String category, final long messageId) {
    	if (!ProductCategory.LEVEL_JOBS.name().toLowerCase().equals(category)) {
    		throw new NoSuchElementException(String.format("Category %s not available for processing", category));
    	} else if (messageId < 0) {
    		throw new IllegalArgumentException(String.format("Message id value %d is out of range", messageId));			
    	}		
    	return getProcessingMsgId() != Status.PROCESSING_MSG_ID_UNDEFINED && getProcessingMsgId() == messageId;
    }
}
