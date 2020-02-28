package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerService;

public class PollingTrigger {
	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(PollingTrigger.class);
	
	private final IngestionTriggerService ingestionTriggerService;

	public PollingTrigger(IngestionTriggerService ingestionTriggerService) {
		this.ingestionTriggerService = ingestionTriggerService;
	}
	
    @Scheduled(fixedRateString = "${ingestion-trigger.polling-interval-ms}")   
	public void poll() {
    	LOG.debug("Trigger polling of all inboxes");
    	ingestionTriggerService.pollAll();
	}	

}
