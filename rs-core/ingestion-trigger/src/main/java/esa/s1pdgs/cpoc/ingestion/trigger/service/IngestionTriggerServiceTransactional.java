/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

	public Set<InboxEntry> getAllForPath(final String pickupURL, final String stationName, final String missionId, final ProductFamily productFamily) {
		final List<InboxEntry> result = this.repository.findByProcessingPodAndPickupURLAndStationNameAndMissionIdAndProductFamily(
				this.processConfiguration.getHostname(), pickupURL, stationName, missionId, productFamily.name());
		LOG.trace("listing persisted inbox entries for inbox {}, station {}, missionId {} and product family {}: {}",
				pickupURL, stationName, missionId, productFamily, result);
		return new HashSet<>(result);
	}

	public Set<InboxEntry> getAllForPath(final String pickupURL, final String stationName, final String missionId) {
		final List<InboxEntry> result = this.repository.findByProcessingPodAndPickupURLAndStationNameAndMissionId(
				this.processConfiguration.getHostname(), pickupURL, stationName, missionId);
		LOG.trace("listing persisted inbox entries for inbox {}, station {} and missionId {}: {}",
				pickupURL, stationName, missionId, result);
		return new HashSet<>(result);
	}

	public void removeFinished(final Collection<InboxEntry> finishedEntries) {
		LOG.trace("deleting inbox entries: {}", finishedEntries);
		this.repository.deleteAll(finishedEntries);
	}

	public InboxEntry add(final InboxEntry entry) {
		Objects.requireNonNull(entry, "InboxEntry must not be null");
		if (null == entry.getKnownSince()) {
			entry.setKnownSince(LocalDateTime.now(ZoneOffset.UTC));
		}
		LOG.debug("persisting inbox entry: {}", entry);
		return this.repository.save(entry);
	}
}
