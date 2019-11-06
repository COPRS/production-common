package esa.s1pdgs.cpoc.reqrepo.status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.status.AbstractAppStatus;
import esa.s1pdgs.cpoc.status.Status;

@Component
public class AppStatusImpl extends AbstractAppStatus {

    /**
     * Logger
     */
    private static final Logger LOGGER = LogManager.getLogger(AppStatusImpl.class);

    /**
     * Constructor
     * 
     * @param maxErrorCounter
     */
    @Autowired
    public AppStatusImpl(@Value("${status.max-error-counter}") final int maxErrorCounter) {
    	super(new Status(maxErrorCounter, 0));
    }

    /**
     * Stop the application if someone asks for forcing stop
     */
    @Scheduled(fixedDelayString = "${status.delete-fixed-delay-ms}")
    public void forceStopping() {
        if (isShallBeStopped()) {
            System.exit(0);
        }
    }
}
