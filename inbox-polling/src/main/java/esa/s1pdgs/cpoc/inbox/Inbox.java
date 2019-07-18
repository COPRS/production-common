package esa.s1pdgs.cpoc.inbox;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.inbox.polling.InboxAdapter;
import esa.s1pdgs.cpoc.inbox.polling.InboxEntry;
import esa.s1pdgs.cpoc.inbox.polling.filter.InboxFilter;
import esa.s1pdgs.cpoc.inbox.polling.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.inbox.polling.repo.PickupContentConverter;
import esa.s1pdgs.cpoc.inbox.polling.repo.PickupContentRepository;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public final class Inbox {	
	private static final Logger LOG = LoggerFactory.getLogger(Inbox.class);
	
	private final InboxAdapter inboxAdapter;
	private final InboxFilter filter;
	private final PickupContentRepository pickupContentRepository;
	private final PickupContentConverter converter;
	private final SubmissionClient client;
	
	public Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter, 
			final PickupContentRepository pickupContentRepository,
			final PickupContentConverter converter,
			final SubmissionClient client
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.pickupContentRepository = pickupContentRepository;
		this.converter = converter;
		this.client = client;
	}
	
	private final Set<InboxEntry> existingContent() {		
		return StreamSupport.stream(pickupContentRepository.findAll().spliterator(), false)
			.map(p -> converter.toInboxEntry(p))
			.collect(Collectors.toCollection(HashSet::new));
	}
	
	public final void poll() {
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
				pickupContentRepository.deleteByUrl(entry.getUrl());				
			}	
			
			// all products not stored in the repo are considered new and shall be added to the 
			// configured queue.
			for (final InboxEntry entry : newElements) {		
				LOG.info("Publishing new entry to kafka queue: {}", entry);
				client.publish(new IngestionDto(entry.getName(), entry.getUrl()));
				
				LOG.debug("Adding {}", entry);
				pickupContentRepository.save(converter.toPickupContent(entry));	
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
