package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.common.utils.Retries;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.PositiveFileSizeFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.name.ProductNameEvaluator;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingInput;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingOutput;
import esa.s1pdgs.cpoc.ingestion.trigger.service.IngestionTriggerServiceTransactional;
import esa.s1pdgs.cpoc.message.MessageProducer;
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
	private final MessageProducer<IngestionJob> messageProducer;
	private final String topic;
	private final ProductFamily family;
	private final String stationName;
	private final String mode;
	private final String timeliness;
	private final ProductNameEvaluator nameEvaluator;
	private final int stationRetentionTime;
	private final int publishMaxRetries;
    private final long publishTempoRetryMs;

	Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter,
			final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional,
			final MessageProducer<IngestionJob> messageProducer,
			final String topic,
			final ProductFamily family,
			final String stationName,
			final int stationRetentionTime,
			final String mode,
			final String timeliness,
			final ProductNameEvaluator nameEvaluator,
			final int publishMaxRetries,
			final long publishTempoRetryMs
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.ingestionTriggerServiceTransactional = ingestionTriggerServiceTransactional;
		this.messageProducer = messageProducer;
		this.topic = topic;
		this.family = family;
		this.stationName = stationName;
		this.stationRetentionTime = stationRetentionTime;
		this.mode = mode;
		this.timeliness = timeliness;
		this.nameEvaluator = nameEvaluator;
		this.publishMaxRetries = publishMaxRetries;
		this.publishTempoRetryMs = publishTempoRetryMs;
		this.log = LoggerFactory.getLogger(String.format("%s (%s) for %s", getClass().getName(), stationName, family));
	}
	
	public final void poll() {
		try {
			//This is a dirty workaround to support backwards compatibility because product family is new and
			//existing InboxEntries for xbip etc. (excl. auxip) do not have product family yet
			//This (else part) can be removed in the future as then all InboxEntries will have product family property
			final PollingRun pollingRun;
			if(inboxAdapter instanceof SupportsProductFamily) {
				pollingRun = PollingRun.newInstance(
						ingestionTriggerServiceTransactional.getAllForPath(inboxAdapter.inboxURL(), stationName, family),
						inboxAdapter.read(new PositiveFileSizeFilter()), stationRetentionTime
				);
			} else {
				pollingRun = PollingRun.newInstanceWithoutProductFamily( // omitting product family comparison S1PRO-2395
						ingestionTriggerServiceTransactional.getAllForPath(inboxAdapter.inboxURL(), stationName),
						inboxAdapter.read(new PositiveFileSizeFilter()), stationRetentionTime
				);
			}

			// when a product has been removed from the inbox directory, it shall be removed
			// from the persistence so it will not be ignored if it occurs again on the inbox
			// S1PRO-2470: additional condition for removal from persistence is the stationRetentionTime
			ingestionTriggerServiceTransactional.removeFinished(pollingRun.finishedElements());
			
			final Set<InboxEntry> handledElements = new HashSet<>();

			for (final InboxEntry newEntry : pollingRun.newElements()) {
				// omit files in subdirectories of already matched products
				if (!isChildOf(newEntry, handledElements)) {
					handleEntry(newEntry).
							ifPresent(handledElements::add);
				}
				persist(newEntry);
			}
			inboxAdapter.advanceAfterPublish();
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
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + ", messageProducer=" + messageProducer + "]";
	}

	private boolean isChildOf(final InboxEntry entry, final Set<InboxEntry> handledElements) {
		final Path thisPath = Paths.get(entry.getRelativePath());

		for (final InboxEntry handledEntry : handledElements) {
			// is child ?
			if (thisPath.startsWith(Paths.get(handledEntry.getRelativePath()))) {
				return true;
			}
		}
		return false;
	}

	final Optional<InboxEntry> handleEntry(final InboxEntry entry) {
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.newReporting("IngestionTrigger");

		final String productName;
		if ("auxip".equalsIgnoreCase(entry.getInboxType())) {
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
			final String publishedName = nameEvaluator.evaluateFrom(entry);
			log.debug("Publishing new entry {} to kafka queue: {}", publishedName, entry);
			publishWithRetries(
					new IngestionJob(
						family, 
						publishedName,
						entry.getPickupURL(), 
						entry.getRelativePath(), 
						entry.getSize(),
						reporting.getUid(),
						stationName,
						mode,
						timeliness,
						entry.getInboxType()
					)
			);
			reporting.end(
					new IngestionTriggerReportingOutput(entry.getPickupURL() + "/" + entry.getRelativePath()), 
					new ReportingMessage("File %s created IngestionJob", productName)
			);
			return Optional.of(entry);
		} catch (final Exception e) {
			reporting.error(new ReportingMessage("File %s could not be handled: %s", productName, LogUtils.toString(e)));
			log.error(String.format("Error on handling %s in %s: %s", entry, description(), LogUtils.toString(e)));
		}
		return Optional.empty();
	}
	
	private void publishWithRetries(IngestionJob ingestionJob) throws InterruptedException {
		Retries.performWithRetries(
				() -> {	this.publish(ingestionJob); return null;}, 
    			"Publishing of IngestionJob for " + ingestionJob.getProductName(),
    			publishMaxRetries,
    			publishTempoRetryMs
		);
	}

	private void publish(IngestionJob ingestionJob) {
		try {
			messageProducer.send(topic, ingestionJob);
		} catch (final Exception e) {
			throw new RuntimeException(
					String.format(
							"Error on publishing IngestionJob for %s to %s: %s",
							ingestionJob.getProductName(),
							topic,
							Exceptions.messageOf(e)
					),
					e
			);
		}
	}

	private InboxEntry persist(final InboxEntry toBePersisted) {
		final InboxEntry persisted = ingestionTriggerServiceTransactional.add(toBePersisted);
		log.trace("Added {} to persistence", persisted);
		return persisted;
	}
}
