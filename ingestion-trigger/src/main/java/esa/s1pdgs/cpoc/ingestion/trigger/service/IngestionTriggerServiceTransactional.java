package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;

@Component
public class IngestionTriggerServiceTransactional {

	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerServiceTransactional.class);

	private final InboxEntryRepository repository;
	private final ProcessConfiguration processConfiguration;

	@Autowired
	public IngestionTriggerServiceTransactional(final InboxEntryRepository repository, ProcessConfiguration processConfiguration) {
		this.repository = repository;
		this.processConfiguration = processConfiguration;
	}

	public Set<InboxEntry> getAllForPath(final String pickupURL, final String stationName, final ProductFamily productFamily) {
		final List<InboxEntry> result = this.repository.findByProcessingPodAndPickupURLAndStationNameAndProductFamily(
				this.processConfiguration.getHostname(), pickupURL, stationName, productFamily.name());
		LOG.debug("listing persisted inbox entries for inbox " + pickupURL + ", station " + stationName
				+ " and product family " + productFamily + ": " + result);
		return new HashSet<>(result);
	}

	public void removeFinished(final Collection<InboxEntry> finishedEntries) {
		LOG.debug("deleting inbox entries: " + finishedEntries);
		this.repository.deleteAll(finishedEntries);
	}

	public InboxEntry add(final InboxEntry entry) {
		LOG.debug("persisting inbox entry: " + entry);
		return this.repository.save(entry);
	}
}
