package esa.s1pdgs.cpoc.ipf.execution.worker.status;

import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

@Component
@Profile("test")
public class TestAppStatusImpl extends AbstractAppStatus {
    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

    @Autowired
    public TestAppStatusImpl(
            @Value("${status.max-error-counter-processing:100}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi:100}") final int maxErrorCounterNextMessage,
            @Qualifier("systemExitCall") final Runnable systemExitCall,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage), systemExitCall);
        this.mqiStatusService = mqiStatusService;
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

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms:3000}")
    public void forceStopping() {
    	// do nothing - otherwise this will break tests
    }

}
