package esa.s1pdgs.cpoc.appstatus;

import org.springframework.scheduling.annotation.Scheduled;


public class DefaultAppStatusImpl extends AbstractAppStatus {

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
            System.exit(0);
        }
    }
}