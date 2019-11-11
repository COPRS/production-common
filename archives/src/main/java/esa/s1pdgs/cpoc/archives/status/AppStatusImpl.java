package esa.s1pdgs.cpoc.archives.status;

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
    		@Value("${status.max-error-counter-reports}") final int maxErrorCounterReports) {
    	super(new Status(maxErrorCounterReports, 0));
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