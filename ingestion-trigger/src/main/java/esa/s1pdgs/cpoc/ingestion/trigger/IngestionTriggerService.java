package esa.s1pdgs.cpoc.ingestion.trigger;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IngestionTriggerService {		
	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerService.class);
	
	private final List<Inbox> inboxes;
	
	public IngestionTriggerService(List<Inbox> inboxes) {
		this.inboxes = inboxes;
	}
	 
	public final void pollAll() {
    	LOG.trace("Polling all");
    	for (final Inbox inbox : inboxes) {
        	LOG.trace("Polling {}", inbox.description());   
        	try {
				inbox.poll();
			} catch (Exception e) {
				LOG.error(String.format("Failed polling %s", inbox), e);
			}
    	}
      	LOG.trace("Done polling all");
	}	
}
