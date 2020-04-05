package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;

@Component
public class IngestionTriggerServiceTransactional {
	private final InboxEntryRepository repository;

	@Autowired
	public IngestionTriggerServiceTransactional(final InboxEntryRepository repository) {
		this.repository = repository;
	}	
	
	public Set<InboxEntry> getAllForPath(final String pickupURL) {
		return repository.findByPickupURL(pickupURL).stream()
				.collect(Collectors.toCollection(HashSet::new));
	}
	
	public void removeFinished(final Collection<InboxEntry> finishedEntries) {		
		repository.deleteAll(finishedEntries);
	}	
	
	public InboxEntry add(final InboxEntry entry) {
		return repository.save(entry);		
	}
}
