package esa.s1pdgs.cpoc.ingestion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

public class PollingTrigger {
	static final Logger LOG = LogManager.getLogger(PollingTrigger.class);
	
	private final IngestionService service;

	public PollingTrigger(IngestionService service) {
		this.service = service;
	}
	
    @Scheduled(fixedRateString = "${ingestion.polling-interval-ms}")   
	public void poll() {
    	LOG.trace("Trigger polling of MQI");
    	service.poll();
	}

}
