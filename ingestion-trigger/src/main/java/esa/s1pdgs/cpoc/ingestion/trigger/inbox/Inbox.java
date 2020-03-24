package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

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
					.getAllForPath(inboxAdapter.inboxPath());
						
			// read all entries that have not been persisted before
			final Set<InboxEntry> pickupContent = new HashSet<>(inboxAdapter.read(InboxFilter.ALLOW_ALL));
			
			// determine the entries that have been deleted from inbox to remove them from persistence
			final Set<InboxEntry> finishedElements = new HashSet<>(persistedContent);
			finishedElements.removeAll(pickupContent);
			
			final StringBuilder logMessage = new StringBuilder();
			if (!finishedElements.isEmpty()) {
				logMessage.append("Handled ")
					.append(finishedElements.size())
					.append(" finished elements (")
					.append(summarize(finishedElements))
					.append(")");
				// when a product has been removed from the inbox directory, it shall be removed
				// from the persistence so it will not be ignored if it occurs again on the inbox
				LOG.debug("Deleting all {} from persistence", summarize(finishedElements));
				ingestionTriggerServiceTransactional.removeFinished(finishedElements);
			}
			
			final Set<InboxEntry> newElements = new HashSet<>(persistedContent);
			newElements.removeAll(persistedContent);

			for (final InboxEntry newEntry : newElements) {
				if (filter.accept(newEntry)) {
					LOG.debug("adding {}", newEntry);	
					handleEntry(newEntry);	
				}
				else {
					LOG.info("{} is ignored by {}", newEntry, filter);
				}	
				persist(newEntry);
			}

			if (!newElements.isEmpty()) {
				if (logMessage.length() == 0) {
					logMessage.append("Handled ");
				} else {
					logMessage.append(" and ");
				}
				logMessage.append(newElements.size())
					.append(" new elements (")
					.append(summarize(newElements))
					.append(")");	
			}
			if (logMessage.length() != 0) {
				LOG.info(logMessage.toString());
			}
		} catch (final Exception e) {			
			// thrown on error reading the Inbox. No real retry here as it will be retried on next polling attempt anyway	
			LOG.error(String.format("Error on polling %s", description()), e);
		}
	}

	public final String description() {
		return inboxAdapter.description() + " (" + filter + ")";
	}

	@Override
	public final String toString() {
		return "Inbox [inboxAdapter=" + inboxAdapter + ", filter=" + filter + ", client=" + client + "]";
	}
	
	private final void handleEntry(final InboxEntry entry) {
		
		// FIXME: dirty workaround, file size and last modified time need to be abstracted when using http://
		// instead of file://
		URI pickupURL;
		try {
			pickupURL = new URI(entry.getPickupPath());
		} catch (final URISyntaxException e) {
			LOG.error("URL syntax not correct for {}", entry.getName());
			return;
		}
		
		final File file = Paths.get(pickupURL).resolve(entry.getRelativePath()).toFile();
		final String filename = file.getName();
		
		final Reporting reporting = ReportingUtils.newReportingBuilder()
				.newReporting("IngestionTrigger");
			
		reporting.begin(
				new IngestionTriggerReportingInput(file.getName(), new Date(), new Date(file.lastModified())),
				new ReportingMessage("New file detected %s", filename)
		);
		
		// empty files are not accepted!
		if (FileUtils.sizeOf(file) == 0) {	
			reporting.error(new ReportingMessage("File %s is empty, ignored.", filename));						
			return;
		}
		
		try {
			LOG.debug("Publishing new entry to kafka queue: {}", entry);					
			final IngestionJob dto = new IngestionJob(family, entry.getName());
		    dto.setRelativePath(entry.getRelativePath());
		    dto.setPickupBaseURL(entry.getPickupPath());
		    dto.setProductName(entry.getName());
		    dto.setUid(reporting.getUid());
			client.publish(dto);	
			reporting.end(
					new IngestionTriggerReportingOutput("file://" + file.getAbsolutePath()), 
					new ReportingMessage("File %s created IngestionJob", filename)
			);
		} catch (final Exception e) {
			reporting.error(new ReportingMessage("File %s could not be handled: %s", filename, LogUtils.toString(e)));
			LOG.error(String.format("Error on handling %s in %s: %s", entry, description(), LogUtils.toString(e)));
		}		
	}
	
	private final InboxEntry persist(final InboxEntry toBePersisted) {
		final InboxEntry persisted = ingestionTriggerServiceTransactional.add(toBePersisted);
		LOG.debug("Added {} to persistence", persisted);
		return persisted;
	}
	
	private final String summarize(final Collection<InboxEntry> entries) {
		final String summary = entries.stream()
			.map(p -> p.getName())
			.collect(Collectors.joining(", "));
		
		if (StringUtils.isEmpty(summary)) {
			return "[none]";
		}
		return summary;
	}
}
