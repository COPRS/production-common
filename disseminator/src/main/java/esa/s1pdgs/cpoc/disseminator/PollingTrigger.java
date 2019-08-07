package esa.s1pdgs.cpoc.disseminator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import esa.s1pdgs.cpoc.disseminator.service.DisseminationService;

public class PollingTrigger {
	static final Logger LOG = LogManager.getLogger(PollingTrigger.class);
	
	private final DisseminationService service;

	public PollingTrigger(DisseminationService service) {
		this.service = service;
	}
	
    @Scheduled(fixedRateString = "${dissemination.polling-interval-ms}")   
	public void poll() {
    	LOG.debug("Trigger polling of MQI");
    	service.poll();
	}
}
