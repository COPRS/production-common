package esa.s1pdgs.cpoc.ingestion.trigger.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;

@Component
@Profile("!test")
public class AppStatusImpl extends AbstractAppStatus {
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    private long failAfterInactivitySeconds;
    
    @Autowired
    public AppStatusImpl(
            @Value("${status.max-error-counter-processing:100}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi:100}") final int maxErrorCounterNextMessage,
            @Value("${status.fail-after-inactivity-for-seconds:7200}") final long failAfterInactivitySeconds,
            @Qualifier("systemExitCall") final Runnable systemExitCall
    ) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage), systemExitCall);
    	this.failAfterInactivitySeconds = failAfterInactivitySeconds;
    }

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Override
	@Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms:3000}")
    public void forceStopping() {
        if (isShallBeStopped()) {
        	LOGGER.info("DefaultAppStatusImpl is doing a forced termination of the application.");
        	systemExit();
        }
    }

	@Override
	public synchronized Status getStatus() {
		final Status status = super.getStatus();		
		if (System.currentTimeMillis() - status.getDateLastChangeMs() > (failAfterInactivitySeconds * 1000L)) {
			LOGGER.info("DefaultAppStatusImpl is doing a forced termination of the application.");
			status.setFatalError();
		}		
		return status;
	}
}
