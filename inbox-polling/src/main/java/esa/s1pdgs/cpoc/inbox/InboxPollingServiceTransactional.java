package esa.s1pdgs.cpoc.inbox;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.entity.InboxEntryRepository;

@Component
@Transactional
public class InboxPollingServiceTransactional {	
	private final InboxEntryRepository repository;

	@Autowired
	public InboxPollingServiceTransactional(InboxEntryRepository repository) {
		this.repository = repository;
	}	
	
	public Set<InboxEntry> getAll() {
		return StreamSupport.stream(repository.findAll().spliterator(), false)
				.collect(Collectors.toCollection(HashSet::new));
	}
	
	public void removeFinished(final Collection<InboxEntry> finishedEntries) {		
		final Set<String> toBeDeleted = finishedEntries.stream()
				.map(e -> e.getUrl())
				.collect(Collectors.toCollection(HashSet::new));
		
		repository.deleteByUrlIn(toBeDeleted);
	}	
	
	public InboxEntry add(final InboxEntry entry) {
		return repository.save(entry);		
	}
}
