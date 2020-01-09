package esa.s1pdgs.cpoc.ipf.execution.worker.status;

import java.util.NoSuchElementException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.mqi.client.StatusService;

@Component
public class AppStatusImpl extends AbstractAppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    /**
     * MQI service for stopping the MQI
     */
    private final StatusService mqiStatusService;

    @Autowired
    public AppStatusImpl(
            @Value("${status.max-error-counter-processing:100}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi:100}") final int maxErrorCounterNextMessage,
            @Qualifier("mqiServiceForStatus") final StatusService mqiStatusService) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage));
        this.mqiStatusService = mqiStatusService;
    }

    @Override
    public boolean isProcessing(String category, long messageId) {
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
        if (isShallBeStopped()) {
            try {
                mqiStatusService.stop();
            } catch (AbstractCodedException ace) {
                LOGGER.error(ace.getLogMessage());
            }
            System.exit(0);
        }
    }

}
