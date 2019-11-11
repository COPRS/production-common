package esa.s1pdgs.cpoc.scaler.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.appstatus.AbstractAppStatus;
import esa.s1pdgs.cpoc.appstatus.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    @Autowired
    public AppStatusImpl(
            @Value("${status.max-error-counter-processing:100}") final int maxErrorCounterProcessing,
            @Value("${status.max-error-counter-mqi:100}") final int maxErrorCounterNextMessage) {
    	super(new Status(maxErrorCounterProcessing, maxErrorCounterNextMessage));
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