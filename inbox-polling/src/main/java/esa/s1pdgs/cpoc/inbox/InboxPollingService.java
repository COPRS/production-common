package esa.s1pdgs.cpoc.inbox;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

// make scheduling configurable via property to allow disabling it in unit tests
@ConditionalOnProperty(value = "scheduling.enable", havingValue = "true", matchIfMissing = true)
@EnableScheduling
public class InboxPollingService {		
	private static final Logger LOG = LoggerFactory.getLogger(InboxPollingService.class);
	
	private final List<Inbox> inboxes;
	
	public InboxPollingService(List<Inbox> inboxes) {
		this.inboxes = inboxes;
	}
	
    @Scheduled(fixedRateString = "${inbox.polling-interval-ms}")    
	public void pollAll() {
    	LOG.trace("Polling all");
    	for (final Inbox inbox : inboxes) {
        	LOG.debug("Polling {}", inbox.description());   
        	try {
				inbox.poll();
			} catch (Exception e) {
				LOG.error(String.format("Failed polling %s", inbox), e);
			}
    	}
      	LOG.trace("Done polling all");
	}	
}
