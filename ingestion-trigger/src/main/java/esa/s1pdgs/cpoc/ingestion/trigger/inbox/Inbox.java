package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingInput;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingOutput;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class Inbox {
	// just make sure top level dirs are handled first
//	static final Comparator<InboxEntry> COMP = new Comparator<InboxEntry>() {
//		@Override
//		public final int compare(final InboxEntry o1, final InboxEntry o2) {			
//			return Paths.get(o1.getRelativePath()).getNameCount() - Paths.get(o2.getRelativePath()).getNameCount();
//		}		
//	};
	
	
	private final Logger log;
	
	private final InboxAdapter inboxAdapter;
	private final InboxFilter filter;
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final SubmissionClient client;
	private final ProductFamily family;
	private final String stationName;
	private final ProductNameEvaluator nameEvaluator;

	Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter,
			final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional, 
			final SubmissionClient client,
			final ProductFamily family,
			final String stationName,
			final ProductNameEvaluator nameEvaluator
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.ingestionTriggerServiceTransactional = ingestionTriggerServiceTransactional;
		this.client = client;
		this.family = family;
		this.stationName = stationName;
		this.nameEvaluator = nameEvaluator;
		this.log = LoggerFactory.getLogger(String.format("%s (%s) for %s", getClass().getName(), stationName, family));
	}
	
	static final class PollingRun {
		private final Set<InboxEntry> persistedContent;
		private final Set<InboxEntry> pickupContent;
		private final Set<InboxEntry> finishedElements;
		private final List<InboxEntry> newElements;
		
		PollingRun(
				final Set<InboxEntry> persistedContent, 
				final Set<InboxEntry> pickupContent,
				final Set<InboxEntry> finishedElements, 
				final List<InboxEntry> newElements
		) {
			this.persistedContent = persistedContent;
			this.pickupContent = pickupContent;
			this.finishedElements = finishedElements;
			this.newElements = newElements;
		}
		
		static PollingRun newInstance(final Set<InboxEntry> persistedContent, final List<InboxEntry> pickupContent) {
			// determine the entries that have been deleted from inbox to remove them from persistence
			final Set<InboxEntry> finishedElements = new HashSet<>(persistedContent);
			finishedElements.removeAll(pickupContent);
			
			// detect all elements that are considered "new" on the inbox
			final List<InboxEntry> newElements = new ArrayList<>(pickupContent);
			newElements.removeAll(persistedContent);
			
			return new PollingRun(
					persistedContent, 
					new HashSet<>(pickupContent), 
					finishedElements, 
					newElements
			);
		}

		public Set<InboxEntry> finishedElements() {
			return finishedElements;
		}		
		
		public List<InboxEntry> newElements() {	
			return newElements;
		}	
		
		private final void dumpTo(final Set<InboxEntry> handledElements, final Logger log) {
			final StringBuilder logMessage = new StringBuilder();
			
			if (!finishedElements.isEmpty()) {			
				logMessage.append("Handled ")
					.append(finishedElements.size())
					.append(" finished elements");
				
				dumpToDebugLog(log, "deleting {} from persistence",finishedElements);			
			}
			
			if (!newElements.isEmpty()) {
				final Set<InboxEntry> ignoredElements = new HashSet<>(newElements);
				ignoredElements.removeAll(handledElements);
				
				if (logMessage.length() == 0) {
					logMessage.append("Handled ");
				} else {
					logMessage.append(" and ");
				}	
				logMessage.append(newElements.size())
					.append(" new elements (processed: ")
					.append(handledElements.size())
					.append(" / ignored: ")
					.append(ignoredElements.size())
					.append(").");
				
				if (!handledElements.isEmpty()) {
					dumpToDebugLog(log, "processed {} from inbox", handledElements);		
				}				
				if (!ignoredElements.isEmpty()) {
					dumpToDebugLog(log, "ignored {} in inbox", ignoredElements);		
				}
			}		
			if (logMessage.length() != 0) {
				log.info(logMessage.toString());
			}
		}
		
		private final void dumpToDebugLog(final Logger log, final String logTemplate, final Collection<InboxEntry> entries) {
			if (log.isDebugEnabled()) {
				entries.stream()
					.map(p -> p.getRelativePath())
					.forEach(e -> log.debug("PickupAction - " + logTemplate, e));
			}			
		}

		@Override
		public String toString() {
			return "PollingRun [persistedContent=" + persistedContent.size() + ", pickupContent=" + pickupContent.size()
					+ ", finishedElements=" + finishedElements.size() + ", newElements=" + newElements.size() + "]";
		}
	}
	
	public final void poll() {	
		try {
			final PollingRun pollingRun = PollingRun.newInstance(
					ingestionTriggerServiceTransactional.getAllForPath(inboxAdapter.inboxURL(), stationName), 
					inboxAdapter.read(InboxFilter.ALLOW_ALL)
			);
						
			// when a product has been removed from the inbox directory, it shall be removed
			// from the persistence so it will not be ignored if it occurs again on the inbox
			ingestionTriggerServiceTransactional.removeFinished(pollingRun.finishedElements());			
			
			final Set<InboxEntry> handledElements = new HashSet<>();
			
			for (final InboxEntry newEntry : pollingRun.newElements()) {		
				// omit files in subdirectories of already matched products
				if (!isChildOf(newEntry, handledElements)) {
					handleEntry(newEntry).
						ifPresent(e -> handledElements.add(e));	
				}			
				persist(newEntry);
			}		
			pollingRun.dumpTo(handledElements, log);
			log.debug(pollingRun.toString());
		} catch (final Exception e) {			
			// thrown on error reading the Inbox. No real retry here as it will be retried on next polling attempt anyway	
			log.error(String.format("Error on polling %s", description()), e);
		}
	}
	
	private final boolean isChildOf(final InboxEntry entry, final Set<InboxEntry> handledElements) {
		final Path thisPath = Paths.get(entry.getRelativePath());
		
		for (final InboxEntry handledEntry : handledElements) {
			// is child ?
			if (thisPath.startsWith(Paths.get(handledEntry.getRelativePath()))) {
				return true;
			}			
		}
		return false;		
	}

	public final String description() {
		return inboxAdapter.description() + " for productFamily " + family + "";
	}

	@Override
	public final String toString() {
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + ", client=" + client + "]";
	}

	private final Optional<InboxEntry> handleEntry(final InboxEntry entry) {				
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.newReporting("IngestionTrigger");
			
		reporting.begin(
				new IngestionTriggerReportingInput(entry.getName(), new Date(), entry.getLastModified()),
				new ReportingMessage("New file detected %s", entry.getName())
		);
		
		if (!filter.accept(entry)) {
			reporting.end(new ReportingMessage("File %s is ignored by filter.", entry.getName()));						
			return Optional.empty();
		}
		
		// empty files are not accepted!
		if (entry.getSize() == 0) {	
			reporting.error(new ReportingMessage("File %s is empty, ignored.", entry.getName()));						
			return Optional.empty();
		}
		
		try {
			log.debug("Publishing new entry to kafka queue: {}", entry);	
			final String publishedName = nameEvaluator.evaluateFrom(Paths.get(entry.getRelativePath()));
			client.publish(
					new IngestionJob(
						family, 
						publishedName, 
						entry.getPickupURL(), 
						entry.getRelativePath(), 
						entry.getSize(),
						reporting.getUid(),
						stationName
					)					
			);	
			reporting.end(
					new IngestionTriggerReportingOutput(entry.getPickupURL() + "/" + entry.getRelativePath()), 
					new ReportingMessage("File %s created IngestionJob", entry.getName())
			);
			return Optional.of(entry);
		} catch (final Exception e) {
			reporting.error(new ReportingMessage("File %s could not be handled: %s", entry.getName(), LogUtils.toString(e)));
			log.error(String.format("Error on handling %s in %s: %s", entry, description(), LogUtils.toString(e)));
		}	
		return Optional.empty();
	}
	
	private final InboxEntry persist(final InboxEntry toBePersisted) {
		final InboxEntry persisted = ingestionTriggerServiceTransactional.add(toBePersisted);
		log.trace("Added {} to persistence", persisted);
		return persisted;
	}
}
