package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.nio.file.Path;
import java.nio.file.Paths;
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
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingInput;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingOutput;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionJob;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class Inbox {	
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
			log.trace(pollingRun.toString());
		} catch (final Exception e) {			
			// thrown on error reading the Inbox. No real retry here as it will be retried on next polling attempt anyway	
			log.error(String.format("Error on polling %s", description()), e);
		}
	}
	public final String description() {
		return inboxAdapter.description() + " for productFamily " + family + "";
	}

	@Override
	public final String toString() {
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + ", client=" + client + "]";
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
