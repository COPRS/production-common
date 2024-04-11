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

package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.CommonConfigurationProperties;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.metadata.PathMetadataExtractor;
import esa.s1pdgs.cpoc.common.utils.DateUtils;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.cadip.CadipInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.PositiveFileSizeFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingInput;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingOutput;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class Inbox {
	private final Logger log;
	
	private final InboxAdapter inboxAdapter;
	private final InboxFilter filter;
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final ProductFamily family;
	private final String missionId;
	private final String stationName;
	private final String mode;
	private final String timeliness;
	private final ProductNameEvaluator nameEvaluator;
	private final int stationRetentionTime;
    private final PathMetadataExtractor pathMetadataExtractor;
    private final CommonConfigurationProperties commonProperties;

	Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter,
			final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional,
			final ProductFamily family,
			final String missionId,
			final String stationName,
			final int stationRetentionTime,
			final String mode,
			final String timeliness,
			final ProductNameEvaluator nameEvaluator,
			final PathMetadataExtractor pathMetadataExtractor,
			final CommonConfigurationProperties commonProperties
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.ingestionTriggerServiceTransactional = ingestionTriggerServiceTransactional;
		this.family = family;
		this.missionId = missionId;
		this.stationName = stationName;
		this.stationRetentionTime = stationRetentionTime;
		this.mode = mode;
		this.timeliness = timeliness;
		this.nameEvaluator = nameEvaluator;
		this.log = LoggerFactory.getLogger(String.format("%s (%s) for %s", getClass().getName(), stationName, family));
		this.pathMetadataExtractor = pathMetadataExtractor;
		this.commonProperties = commonProperties;
	}
	
	public final List<IngestionJob> poll() {
		try {
			//This is a dirty workaround to support backwards compatibility because product family is new and
			//existing InboxEntries for xbip etc. (excl. auxip) do not have product family yet
			//This (else part) can be removed in the future as then all InboxEntries will have product family property
			final PollingRun pollingRun;
			if(inboxAdapter instanceof SupportsProductFamily) {
				pollingRun = PollingRun.newInstance(
						ingestionTriggerServiceTransactional.getAllForPath(inboxAdapter.inboxURL(), stationName, missionId, family),
						inboxAdapter.read(new PositiveFileSizeFilter()), stationRetentionTime
				);
			} else {
				pollingRun = PollingRun.newInstanceWithoutProductFamily( // omitting product family comparison S1PRO-2395
						ingestionTriggerServiceTransactional.getAllForPath(inboxAdapter.inboxURL(), stationName, missionId),
						inboxAdapter.read(new PositiveFileSizeFilter()), stationRetentionTime
				);
			}

			// when a product has been removed from the inbox directory, it shall be removed
			// from the persistence so it will not be ignored if it occurs again on the inbox
			// S1PRO-2470: additional condition for removal from persistence is the stationRetentionTime
			ingestionTriggerServiceTransactional.removeFinished(pollingRun.finishedElements());
			
			final Set<InboxEntry> handledElements = new HashSet<>();
			final List<IngestionJob> jobs = new ArrayList<>();

			for (final InboxEntry newEntry : pollingRun.newElements()) {
				// omit files in subdirectories of already matched products
				if (!isChildOf(newEntry, handledElements)) {
					Optional<InboxReturnValue> returnValue = handleEntry(newEntry);
					if (returnValue.isPresent()) {
						handledElements.add(returnValue.get().getEntry());
						jobs.add(returnValue.get().getJob());
					}
				}
				persist(newEntry);
			}
			
			inboxAdapter.advanceAfterPublish();
			pollingRun.dumpTo(handledElements, log);
			log.trace(pollingRun.toString());
			
			return jobs;
		} catch (final Exception e) {			
			// thrown on error reading the Inbox. No real retry here as it will be retried on next polling attempt anyway	
			log.error(String.format("Error on polling %s", description()), e);
			return Collections.emptyList();
		}
	}
	public final String description() {
		return inboxAdapter.description() + " for productFamily " + family + "";
	}

	@Override
	public final String toString() {
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + "]";
	}

	private boolean isChildOf(final InboxEntry entry, final Set<InboxEntry> handledElements) {
		// Skip this check for CADIP
		if ("cadip".equalsIgnoreCase(entry.getInboxType())) {
			log.debug("Skip isChildOf-check for CADIP");
			return false;
		}
		
		final Path thisPath = Paths.get(entry.getRelativePath());

		for (final InboxEntry handledEntry : handledElements) {
			// is child ?
			if (thisPath.startsWith(Paths.get(handledEntry.getRelativePath()))) {
				return true;
			}
		}
		return false;
	}

	final Optional<InboxReturnValue> handleEntry(final InboxEntry entry) {
		MissionId mission = null;
		
		if (this.missionId == null) {
			mission = MissionId.fromFileName(entry.getName());
		} else {
			mission = MissionId.valueOf(this.missionId.toUpperCase());
		}
		
		final Reporting reporting = ReportingUtils.newReportingBuilder(mission)
				.rsChainName(commonProperties.getRsChainName())
				.rsChainVersion(commonProperties.getRsChainVersion())
				.newReporting("IngestionTrigger");

		final String productName;
		if ("auxip".equalsIgnoreCase(entry.getInboxType()) || "cadip".equalsIgnoreCase(entry.getInboxType())) {
			productName = entry.getRelativePath();
		} else {
			productName = entry.getName();
		}
		
		final ReportingInput input = IngestionTriggerReportingInput.newInstance(
				productName,
				family,
				entry.getLastModified()
		);
		reporting.begin(input,new ReportingMessage("New file detected %s", productName));
		
		if (!filter.accept(entry)) {
			reporting.end(new ReportingMessage("File %s is ignored by filter.", productName));
			return Optional.empty();
		}

		// empty files are not accepted!
		if (entry.getSize() == 0) {	
			reporting.error(new ReportingMessage("File %s is empty, ignored.", productName));						
			return Optional.empty();
		}
		
		try {
			IngestionJob job = null;
			if ("cadip".equalsIgnoreCase(entry.getInboxType())) {
				/*
				 * Products being queried from CADIP does not have a path in the common sense as the files
				 * are stored flat within the system.
				 */
				log.debug("Publishing new cadip entry {} to kafka queue: {}", productName, entry);
				
				String t0PdgsDate = DateUtils.formatToMetadataDateTimeFormat(
						entry.getLastModified().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

				job = new IngestionJob(ProductFamily.valueOf(entry.getProductFamily()),  entry.getName(), entry.getPickupURL(),
						entry.getRelativePath(), entry.getSize(), entry.getLastModified(), reporting.getUid(),
						mission.name(), stationName, mode, timeliness, entry.getInboxType(),
						null, t0PdgsDate);
			} else {
				/*
				 * This is the classic case where the path name is extracted and added into the 
				 * message.
				 */
				final String publishedName = nameEvaluator.evaluateFrom(entry);

				// S1OPS-971: Use the entire path element for the rule evaluation
				final String absolutePath = absolutePathOf(entry);

				log.debug("Publishing new entry {} to kafka queue: {}", publishedName, entry);

				String t0PdgsDate = DateUtils.formatToMetadataDateTimeFormat(
						entry.getLastModified().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());

				job = new IngestionJob(family, publishedName, entry.getPickupURL(),
						entry.getRelativePath(), entry.getSize(), entry.getLastModified(), reporting.getUid(),
						mission.name(), stationName, mode, timeliness, entry.getInboxType(),
						pathMetadataExtractor.metadataFrom(absolutePath), t0PdgsDate);
			}
			// RS-536: Add RS Chain version to message
			job.setRsChainVersion(commonProperties.getRsChainVersion());
			
			reporting.end(
					new IngestionTriggerReportingOutput(entry.getPickupURL() + "/" + entry.getRelativePath()), 
					new ReportingMessage("File %s created IngestionJob", productName)
			);
			return Optional.of(new InboxReturnValue(entry, job));
		} catch (final Exception e) {
			reporting.error(new ReportingMessage("File %s could not be handled: %s", productName, LogUtils.toString(e)));
			log.error(String.format("Error on handling %s in %s: %s", entry, description(), LogUtils.toString(e)));
		}
		return Optional.empty();
	}
	
	final String absolutePathOf(final InboxEntry entry) throws URISyntaxException {		
		final URI uri = new URIBuilder(entry.getPickupURL()).build();			
		final Path absolutePath = Paths.get(uri.getPath()).resolve(entry.getRelativePath())
				.toAbsolutePath();		
		return absolutePath.toString();
	}

	private InboxEntry persist(final InboxEntry toBePersisted) {
		final InboxEntry persisted = ingestionTriggerServiceTransactional.add(toBePersisted);
		log.trace("Added {} to persistence", persisted);
		return persisted;
	}
	
	/*
	 * Composite class for Inbox-Entry and Ingestion Job 
	 */
	public static final class InboxReturnValue {
		
		private final InboxEntry entry;
		private final IngestionJob job;
		
		public InboxReturnValue(InboxEntry entry, IngestionJob job) {
			super();
			this.entry = entry;
			this.job = job;
		}

		public InboxEntry getEntry() {
			return entry;
		}

		public IngestionJob getJob() {
			return job;
		}
	}
}
