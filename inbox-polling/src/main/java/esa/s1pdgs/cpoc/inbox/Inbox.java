package esa.s1pdgs.cpoc.inbox;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntryRepository;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;
import esa.s1pdgs.cpoc.inbox.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

@Transactional
public class Inbox {	
	private static final Logger LOG = LoggerFactory.getLogger(Inbox.class);
	
	private final InboxAdapter inboxAdapter;
	private final InboxFilter filter;
	private final InboxEntryRepository inboxEntryRepository;
	private final SubmissionClient client;
	
	Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter, 
			final InboxEntryRepository inboxEntryRepository,
			final SubmissionClient client
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.inboxEntryRepository = inboxEntryRepository;
		this.client = client;
	}
	
	private final Set<InboxEntry> existingContent() {		
		return StreamSupport.stream(inboxEntryRepository.findAll().spliterator(), false)
			.collect(Collectors.toCollection(HashSet::new));
	}

	public void poll() {
		try {
			final Set<InboxEntry> pickupContent = new HashSet<>(inboxAdapter.read(filter));
			final Set<InboxEntry> persistedContent = existingContent();

			final Set<InboxEntry> newElements = new HashSet<>(pickupContent);
			newElements.removeAll(persistedContent);
			LOG.debug("Got {} new elements: {}", newElements.size(), newElements);
			
			final Set<InboxEntry> finishedElements = new HashSet<>(persistedContent); 
			finishedElements.removeAll(pickupContent);
			LOG.debug("Got {} finished elements: {}", finishedElements.size(), finishedElements);
			
			// when a product has been removed from the inbox directory, it shall be removed from the
			// persistence so it will not be ignored if it occurs again on the inbox
			for (final InboxEntry entry : finishedElements) {
				LOG.debug("Deleting {}", entry);
				inboxEntryRepository.deleteByUrl(entry.getUrl());
			}	
			
			// all products not stored in the repo are considered new and shall be added to the 
			// configured queue.
			for (final InboxEntry entry : newElements) {		
				LOG.info("Publishing new entry to kafka queue: {}", entry);
				client.publish(new IngestionDto(entry.getName(), entry.getUrl()));				
				LOG.debug("Adding {}", entry);
				inboxEntryRepository.save(entry);		
			}
		} catch (Exception e) {
			LOG.error(String.format("Error on polling %s", description()), e);
		}
	}
	
	public String description() {
		return inboxAdapter.description() + " ("+ filter + ")";
 	}

	@Override
	public String toString() {
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + ", client=" + client + "]";
	}	
	
}
