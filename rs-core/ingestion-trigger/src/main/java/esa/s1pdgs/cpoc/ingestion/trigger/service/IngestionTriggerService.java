package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.Inbox;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;

public final class IngestionTriggerService implements Supplier<List<IngestionJob>>{		
	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerService.class);
	
	private final List<Inbox> inboxes;
	private final AppStatus status;
	private final AtomicLong counter = new AtomicLong(1L);
		
	public IngestionTriggerService(final List<Inbox> inboxes, final AppStatus status) {
		this.inboxes 	= inboxes;
		this.status		= status;
	}
	
	public List<IngestionJob> get() {
		LOG.trace("Polling all");
		
    	List<IngestionJob> jobs = new ArrayList<>();
		
    	for (final Inbox inbox : inboxes) {
        	LOG.debug("Polling {}", inbox.description());   
        	status.setProcessing(counter.incrementAndGet());
        	try {        	
        		List<IngestionJob> inboxJobs = inbox.poll();
				jobs.addAll(inboxJobs);
			} catch (final Exception e) {
				LOG.error(String.format("Failed polling %s", inbox), e);			
			}
        	status.setWaiting();        
    	}
      	LOG.trace("Done polling all");
      	
      	return jobs;
	}
}
