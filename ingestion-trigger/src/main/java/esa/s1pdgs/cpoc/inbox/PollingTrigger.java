package esa.s1pdgs.cpoc.inbox;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

public class PollingTrigger {
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PollingTrigger.class);
	
	private final InboxPollingService pollingService;

	public PollingTrigger(InboxPollingService pollingService) {
		this.pollingService = pollingService;
	}
	
    @Scheduled(fixedRateString = "${inbox.polling-interval-ms}")   
	public void poll() {
    	LOG.debug("Trigger polling of all inboxes");
    	pollingService.pollAll();
	}	

}
