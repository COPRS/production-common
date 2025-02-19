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

package esa.s1pdgs.cpoc.ingestion.trigger.cadip;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.model.CadipFile;
import de.werum.coprs.cadip.client.model.CadipSession;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.config.CadipConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.SupportsProductFamily;

public class CadipInboxAdapter extends AbstractInboxAdapter implements SupportsProductFamily {
	private static final Logger LOG = LogManager.getLogger(CadipInboxAdapter.class);
	public final static String INBOX_TYPE = "cadip";

	private CadipConfiguration configuration;
	private ProcessConfiguration processConfiguration;
	private CadipStateRepository stateRepository;
	private CadipSessionStateRepository sessionRepository;
	private CadipClient cadipClient;
	private String satelliteId;
	private LocalDateTime nextWindowStart;

	public CadipInboxAdapter(InboxEntryFactory inboxEntryFactory, CadipConfiguration configuration,
			ProcessConfiguration processConfiguration, CadipStateRepository stateRepository,
			CadipSessionStateRepository sessionRepository, CadipClient client, URI inboxURL, String stationName,
			String missionId, ProductFamily productFamily, String satelliteId) {
		super(inboxEntryFactory, inboxURL, stationName, missionId, productFamily);

		this.configuration = configuration;
		this.processConfiguration = processConfiguration;
		this.stateRepository = stateRepository;
		this.sessionRepository = sessionRepository;
		this.cadipClient = client;
		this.satelliteId = satelliteId;
	}

	@Override
	protected Stream<EntrySupplier> list() throws IOException {
		// Retrieve list of new sessions
		final CadipState state = retrieveState();

		// Persist next window start for method advanceAfterPublish
		this.nextWindowStart = LocalDateTime.now();

		List<CadipSession> sessions = this.cadipClient.getSessions(state.getSatelliteId(), null,
				LocalDateTime.ofInstant(state.getNextWindowStart().toInstant(), ZoneOffset.UTC));

		// Check if one of the sessions contains a newer date than
		// "this.nextWindowStart"
		for (CadipSession session : sessions) {
			if (session.getPublicationDate().isAfter(this.nextWindowStart)) {
				this.nextWindowStart = session.getPublicationDate();
			}
		}

		// save new sessions to SessionRepository
		saveNewSessionsToRepository(sessions);

		// Retrieve new files for each known session
		List<CadipSessionState> allSessions = this.sessionRepository
				.findByPodAndCadipUrl(this.processConfiguration.getHostname(), inboxURL());

		// When retrieved files have flag FinalBlock, increase number of finished
		// channels. Delete finished sessions from repo, to keep storage small.
		List<CadipFile> newFiles = retrieveNewFilesAndUpdateSessionState(allSessions);

		return newFiles.stream().map(this::toEntrySupplier);
	}

	@Override
	public void advanceAfterPublish() {
		CadipState cadipState = retrieveState();

		cadipState.setNextWindowStart(new Date(this.nextWindowStart.toInstant(ZoneOffset.UTC).toEpochMilli()));

		this.stateRepository.save(cadipState);
	}

	private CadipState retrieveState() {
		final Optional<CadipState> state = this.stateRepository.findByPodAndCadipUrlAndSatelliteId(
				this.processConfiguration.getHostname(), inboxURL(), this.satelliteId);

		if (state.isPresent()) {
			LOG.debug("Retrieving existing CadipState {} from database", state.get());
			return state.get();
		}

		// If none state exists yet, create a new one
		final CadipState newState = new CadipState();
		newState.setNextWindowStart(new Date(Instant.from(
				ZonedDateTime.ofLocal(DateUtils.parse(this.configuration.getStart()), ZoneId.of("UTC"), ZoneOffset.UTC))
				.toEpochMilli()));
		newState.setCadipUrl(inboxURL());
		newState.setPod(this.processConfiguration.getHostname());
		newState.setSatelliteId(this.satelliteId);
		this.stateRepository.save(newState);

		LOG.debug("New CadipState {} stored in database", newState);

		return newState;
	}

	private void saveNewSessionsToRepository(final List<CadipSession> newSessions) {
		// Only insert new Sessions into repository
		for (CadipSession session : newSessions) {
			Optional<CadipSessionState> result = this.sessionRepository.findByPodAndCadipUrlAndSessionIdAndRetransfer(
					this.processConfiguration.getHostname(), inboxURL(), session.getSessionId(),
					session.getRetransfer());

			if (result.isPresent()) {
				// Session is already being processed. No need to save it again
				LOG.warn("CadipSessionState {} already present in database", result.get());
				continue;
			}

			final CadipSessionState newSessionState = new CadipSessionState();
			// For the first time window take the publication date of the session minus 1
			// second. We want to be sure, to get alle files for the session
			newSessionState.setNextWindowStart(
					new Date(session.getPublicationDate().minusSeconds(1).toInstant(ZoneOffset.UTC).toEpochMilli()));
			newSessionState.setCadipUrl(inboxURL());
			newSessionState.setPod(this.processConfiguration.getHostname());
			newSessionState.setSessionId(session.getSessionId());
			newSessionState.setRetransfer(session.getRetransfer());
			newSessionState.setNumChannels(session.getNumChannels().intValue());
			newSessionState.setCompletedChannels(0);

			this.sessionRepository.save(newSessionState);

			LOG.debug("New CadipSessionState {} stored in database", newSessionState);
		}
	}

	private List<CadipFile> retrieveNewFilesAndUpdateSessionState(final List<CadipSessionState> sessionStates) {
		List<CadipFile> newFiles = new ArrayList<>();

		for (CadipSessionState sessionState : sessionStates) {
			LocalDateTime lastIngestionDate = LocalDateTime.ofInstant(sessionState.getNextWindowStart().toInstant(),
					ZoneOffset.UTC);
			LocalDateTime now = LocalDateTime.now();
			List<CadipFile> response = this.cadipClient.getFiles(sessionState.getSessionId(), null,
					sessionState.isRetransfer(), lastIngestionDate);

			// make sure, that the nextTimestamp is correct
			// use "now" but if in the response a publicationDate is greater than now use
			// that one (prevent duplicated processing)
			for (CadipFile file : response) {
				// Check if file was already processed
				if (!file.getPublicationDate().isAfter(lastIngestionDate)) {
					continue;
				}

				if (file.getPublicationDate().isAfter(now)) {
					now = file.getPublicationDate();
				}

				// If the File is the final one for a channel, update number of completed
				// channels
				if (file.getFinalBlock()) {
					sessionState.setCompletedChannels(sessionState.getCompletedChannels() + 1);
				}

				newFiles.add(file);
			}

			// Update sessionState entry with new timeWindow and potentially new number of
			// completed channels
			if (sessionState.getCompletedChannels().equals(sessionState.getNumChannels())) {
				LOG.info("Session ({}, {}) finished detecting - delete sessionState entry", sessionState.getSessionId(),
						sessionState.isRetransfer());
				this.sessionRepository.deleteById(sessionState.getId());
			} else {
				sessionState.setNextWindowStart(new Date(now.toInstant(ZoneOffset.UTC).toEpochMilli()));
				this.sessionRepository.save(sessionState);
			}
		}

		return newFiles;
	}

	private EntrySupplier toEntrySupplier(final CadipFile cadipFile) {
		return new EntrySupplier(Paths.get(cadipFile.getName()), () -> toInboxEntry(cadipFile));
	}

	private InboxEntry toInboxEntry(final CadipFile cadipFile) {
		LOG.debug("handling cadip file: {}", cadipFile.toString());

		return new InboxEntry(cadipFile.getId().toString(), cadipFile.getSessionId() + "/" + cadipFile.getName(),
				inboxURL(), new Date(cadipFile.getPublicationDate().toInstant(ZoneOffset.UTC).toEpochMilli()),
				cadipFile.getSize(), processConfiguration.getHostname(), "cadip",
				cadipFile.getRetransfer() ? configuration.getRetransferFamily().name() : productFamily.name(),
				stationName, missionId);
	}
}
