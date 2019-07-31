package esa.s1pdgs.cpoc.ingestion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class PollingTrigger {
	private static final Logger LOG = LoggerFactory.getLogger(PollingTrigger.class);
	
	private final IngestionService service;

	public PollingTrigger(IngestionService service) {
		this.service = service;
	}
	
    @Scheduled(fixedRateString = "${ingestion.polling-interval-ms}")   
	public void poll() {
    	LOG.debug("Trigger polling of MQI");
    	service.poll();
	}

}
