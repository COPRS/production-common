package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.inbox.Inbox;

public final class IngestionTriggerService {		
	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerService.class);
	
	private final List<Inbox> inboxes;
	
	public IngestionTriggerService(final List<Inbox> inboxes) {
		this.inboxes = inboxes;
	}
	 
	public final void pollAll() {
    	LOG.trace("Polling all");
    	for (final Inbox inbox : inboxes) {
        	LOG.debug("Polling {}", inbox.description());   
        	try {
				inbox.poll();
			} catch (final Exception e) {
				LOG.error(String.format("Failed polling %s", inbox), e);
			}
    	}
      	LOG.trace("Done polling all");
	}	
}
