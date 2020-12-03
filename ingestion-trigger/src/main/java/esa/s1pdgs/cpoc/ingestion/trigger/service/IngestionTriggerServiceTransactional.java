package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;

@Component
public class IngestionTriggerServiceTransactional {

	private final InboxEntryRepository repository;
	private final ProcessConfiguration processConfiguration;

	@Autowired
	public IngestionTriggerServiceTransactional(final InboxEntryRepository repository, ProcessConfiguration processConfiguration) {
		this.repository = repository;
		this.processConfiguration = processConfiguration;
	}

	public Set<InboxEntry> getAllForPath(final String pickupURL, final String stationName, final ProductFamily productFamily) {
		final List<InboxEntry> result = repository.findByProcessingPodAndPickupURLAndStationNameAndProductFamily(
				this.processConfiguration.getHostname(), pickupURL, stationName, productFamily.name());
		return new HashSet<>(result);
	}
	
	public void removeFinished(final Collection<InboxEntry> finishedEntries) {		
		repository.deleteAll(finishedEntries);
	}	
	
	public InboxEntry add(final InboxEntry entry) {
		return repository.save(entry);		
	}
}
