package esa.s1pdgs.cpoc.ingestion.trigger;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

public class PollingTrigger {
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PollingTrigger.class);
	
	private final IngestionTriggerService pollingService;

	public PollingTrigger(IngestionTriggerService pollingService) {
		this.pollingService = pollingService;
	}
	
    @Scheduled(fixedRateString = "${ingestion-trigger.polling-interval-ms}")   
	public void poll() {
    	LOG.debug("Trigger polling of all inboxes");
    	pollingService.pollAll();
	}	

}
