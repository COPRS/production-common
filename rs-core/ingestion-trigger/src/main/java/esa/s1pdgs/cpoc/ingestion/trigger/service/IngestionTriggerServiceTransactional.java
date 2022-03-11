package esa.s1pdgs.cpoc.ingestion.trigger.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.IngestionTriggerConfigurationProperties;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntryRepository;

@Component
public class IngestionTriggerServiceTransactional {

	private static final Logger LOG = LoggerFactory.getLogger(IngestionTriggerServiceTransactional.class);

	private final InboxEntryRepository repository;
	private final ProcessConfiguration processConfiguration;

	@Autowired
	public IngestionTriggerServiceTransactional(final InboxEntryRepository repository, IngestionTriggerConfigurationProperties configuration) {
		this.repository = repository;
		this.processConfiguration = configuration.getProcess();
	}

	public Set<InboxEntry> getAllForPath(final String pickupURL, final String stationName, final String missionId, final ProductFamily productFamily) {
		final List<InboxEntry> result = this.repository.findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(
				this.processConfiguration.getHostname(), pickupURL, stationName, missionId, productFamily.name());
		LOG.debug("listing persisted inbox entries for inbox " + pickupURL + ", station " + stationName + ", missionId " + missionId
				+ " and product family " + productFamily + ": " + result);
		return new HashSet<>(result);
	}

	public Set<InboxEntry> getAllForPath(final String pickupURL, final String stationName, final String missionId) {
		final List<InboxEntry> result = this.repository.findByProcessingPodAndPickupURLAndStationNameAndMissionId(
				this.processConfiguration.getHostname(), pickupURL, stationName, missionId);
		LOG.debug("listing persisted inbox entries for inbox " + pickupURL + ", station " + stationName + " and missionId " + missionId +": " + result);
		return new HashSet<>(result);
	}

	public void removeFinished(final Collection<InboxEntry> finishedEntries) {
		LOG.debug("deleting inbox entries: " + finishedEntries);
		this.repository.deleteAll(finishedEntries);
	}

	public InboxEntry add(final InboxEntry entry) {
		Objects.requireNonNull(entry, "InboxEntry must not be null");
		if (null == entry.getKnownSince()) {
			entry.setKnownSince(LocalDateTime.now(ZoneOffset.UTC));
		}
		LOG.debug("persisting inbox entry: " + entry);
		return this.repository.save(entry);
	}
}
