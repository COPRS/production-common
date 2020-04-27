package esa.s1pdgs.cpoc.appstatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class DefaultAppStatusImpl extends AbstractAppStatus {
	
	private static final Log LOGGER = LogFactory.getLog(DefaultAppStatusImpl.class);

    public DefaultAppStatusImpl(final int maxErrorCounterProcessing, final int maxErrorCounterNextMessage) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage));
    }    

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms:3000}")
    public void forceStopping() {
        if (isShallBeStopped()) {
        	LOGGER.info("DefaultAppStatusImpl is doing a forced termination of the application.");
            System.exit(0);
        }
    }
}