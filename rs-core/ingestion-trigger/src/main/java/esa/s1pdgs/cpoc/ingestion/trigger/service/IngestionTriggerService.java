package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.Inbox;

public final class IngestionTriggerService {		
	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerService.class);
	
	private final List<Inbox> inboxes;
	private final AppStatus status;
	private final AtomicLong counter = new AtomicLong(1L);
		
	public IngestionTriggerService(final List<Inbox> inboxes, final AppStatus status) {
		this.inboxes 	= inboxes;
		this.status		= status;
	}
	 
	public final void pollAll() {
    	LOG.trace("Polling all");

    	for (final Inbox inbox : inboxes) {
        	LOG.debug("Polling {}", inbox.description());   
        	status.setProcessing(counter.incrementAndGet());
        	try {        	
				inbox.poll();
			} catch (final Exception e) {
				LOG.error(String.format("Failed polling %s", inbox), e);			
			}
        	status.setWaiting();        
    	}
      	LOG.trace("Done polling all");
	}	
}
