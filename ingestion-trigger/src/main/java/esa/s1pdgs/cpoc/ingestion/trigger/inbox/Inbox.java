package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingInput;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingOutput;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class Inbox {
	private static final Logger LOG = LoggerFactory.getLogger(Inbox.class);
	
	private final InboxAdapter inboxAdapter;
	private final InboxFilter filter;
	private final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional;
	private final SubmissionClient client;
	private final ProductFamily family;

	Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter,
			final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional, 
			final SubmissionClient client,
			final ProductFamily family
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.ingestionTriggerServiceTransactional = ingestionTriggerServiceTransactional;
		this.client = client;
		this.family = family;
	}
	
	public final void poll() {	
		try {
			final Set<InboxEntry> persistedContent = ingestionTriggerServiceTransactional
					.getAllForPath(inboxAdapter.inboxURL());
						
			// read all entries that have not been persisted before
			final Set<InboxEntry> pickupContent = new HashSet<>(inboxAdapter.read(InboxFilter.ALLOW_ALL));
			
			// determine the entries that have been deleted from inbox to remove them from persistence
			final Set<InboxEntry> finishedElements = new HashSet<>(persistedContent);
			finishedElements.removeAll(pickupContent);
			
			// detect all elements that are considered "new" on the inbox
			final Set<InboxEntry> newElements = new HashSet<>(pickupContent);
			newElements.removeAll(persistedContent);
						
			// when a product has been removed from the inbox directory, it shall be removed
			// from the persistence so it will not be ignored if it occurs again on the inbox
			ingestionTriggerServiceTransactional.removeFinished(finishedElements);			
			
			final Set<InboxEntry> handledElements = new HashSet<>();
			for (final InboxEntry newEntry : newElements) {			
				handleEntry(newEntry).
					ifPresent(e -> handledElements.add(e));				
				persist(newEntry);
			}		
			appendMessagesToLog(finishedElements, newElements, handledElements);

		} catch (final Exception e) {			
			// thrown on error reading the Inbox. No real retry here as it will be retried on next polling attempt anyway	
			LOG.error(String.format("Error on polling %s", description()), e);
		}
	}

	public final String description() {
		return inboxAdapter.description() + " for productFamily " + family + "";
	}

	@Override
	public final String toString() {
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + ", client=" + client + "]";
	}

	private final void appendMessagesToLog(
			final Set<InboxEntry> finishedElements,
			final Set<InboxEntry> newElements,
			final Set<InboxEntry> handledElements
	) {
		final StringBuilder logMessage = new StringBuilder();
		
		if (!finishedElements.isEmpty()) {			
			logMessage.append("Handled ")
				.append(finishedElements.size())
				.append(" finished elements");
			
			dumpToDebugLog("deleting {} from persistence",finishedElements);			
		}
		
		if (!newElements.isEmpty()) {
			final Set<InboxEntry> ignoredElements = new HashSet<>(newElements);
			newElements.removeAll(handledElements);
			
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
				dumpToDebugLog("processed {} from inbox", newElements);		
			}				
			if (!ignoredElements.isEmpty()) {
				dumpToDebugLog("ignored {} in inbox", newElements);		
			}
		}		
		if (logMessage.length() != 0) {
			LOG.info(logMessage.toString());
		}
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
			LOG.debug("Publishing new entry to kafka queue: {}", entry);		    
			client.publish(
					new IngestionJob(
						family, 
						entry.getName(), 
						entry.getPickupURL(), 
						entry.getRelativePath(), 
						entry.getSize(),
						reporting.getUid()
					)					
			);	
			reporting.end(
					new IngestionTriggerReportingOutput(entry.getPickupURL() + "/" + entry.getRelativePath()), 
					new ReportingMessage("File %s created IngestionJob", entry.getName())
			);
			return Optional.of(entry);
		} catch (final Exception e) {
			reporting.error(new ReportingMessage("File %s could not be handled: %s", entry.getName(), LogUtils.toString(e)));
			LOG.error(String.format("Error on handling %s in %s: %s", entry, description(), LogUtils.toString(e)));
		}	
		return Optional.empty();
	}
	
	private final InboxEntry persist(final InboxEntry toBePersisted) {
		final InboxEntry persisted = ingestionTriggerServiceTransactional.add(toBePersisted);
		LOG.trace("Added {} to persistence", persisted);
		return persisted;
	}
	
//	private final String summarizeAndLog(final String logTemplate, final Collection<InboxEntry> entries) {
//		final String summary = entries.stream()
//			.map(e -> extractNameAndLog(logTemplate,e))
//			.collect(Collectors.joining(", "));
//		
//		if (StringUtils.isEmpty(summary)) {
//			return "[none]";
//		}
//		return summary;
//	}
//	
//	private static String extractNameAndLog(final String logTemplate, final InboxEntry entry) {
//		final String name = entry.getName();
//		LOG.debug("PickupAction - " + logTemplate, name);
//		return name;
//	}
	
	private static final void dumpToDebugLog(final String logTemplate, final Collection<InboxEntry> entries) {
		if (LOG.isDebugEnabled()) {
			entries.stream()
				.map(p -> p.getName())
				.forEach(e -> LOG.debug("PickupAction - " + logTemplate, e));
		}
		
	}
	
}
