package esa.s1pdgs.cpoc.ingestion.trigger;

import java.io.File;
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

import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingInput;
import esa.s1pdgs.cpoc.ingestion.trigger.report.IngestionTriggerReportingOutput;
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

	Inbox(
			final InboxAdapter inboxAdapter, 
			final InboxFilter filter,
			final IngestionTriggerServiceTransactional ingestionTriggerServiceTransactional, 
			final SubmissionClient client
	) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.ingestionTriggerServiceTransactional = ingestionTriggerServiceTransactional;
		this.client = client;
	}
	
	public final void poll() {
		try {
			final Set<InboxEntry> pickupContent = new HashSet<>(inboxAdapter.read(filter));
			final Set<InboxEntry> persistedContent = ingestionTriggerServiceTransactional
					.getAllForPath(inboxAdapter.inboxPath());

			LOG.trace("Found {} on pickup and persisted are {}", summarize(pickupContent), summarize(persistedContent));
			final Set<InboxEntry> newElements = new HashSet<>(pickupContent);
			newElements.removeAll(persistedContent);	
			
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
				
				// all products not stored in the repo are considered new and shall be added to
				// the configured queue.
				for (final InboxEntry entry : newElements) {
					// TODO add retries					
					final File file = Paths.get(entry.getPickupPath(), entry.getRelativePath()).toFile();
					final String filename = file.getName();
					
					final Reporting reporting = ReportingUtils.newReportingBuilder()
							.newReporting("IngestionTrigger");
						
					reporting.begin(
							new IngestionTriggerReportingInput(file.getName(), new Date(), new Date(file.lastModified())),
							new ReportingMessage("New file detected %s", filename)
					);
					
					if (FileUtils.sizeOf(file) == 0) {	
						reporting.error(new ReportingMessage("File %s is empty, ignored.", filename));						
						// ensure that the empty file is persisted so it is not found again
						final InboxEntry persisted = ingestionTriggerServiceTransactional.add(entry);
						LOG.debug("Added {} to persistence", persisted);
						continue;
					}
					
					try {
						LOG.debug("Publishing new entry to kafka queue: {}", entry);					
						final IngestionJob dto = new IngestionJob(entry.getName());
					    dto.setRelativePath(entry.getRelativePath());
					    dto.setPickupPath(entry.getPickupPath());
					    dto.setUid(reporting.getUid());
						client.publish(dto);
						persist(entry);		
						reporting.end(
								new IngestionTriggerReportingOutput("file://" + file.getAbsolutePath()), 
								new ReportingMessage("File %s created IngestionJob", filename)
						);
					} catch (final Exception e) {
						reporting.error(new ReportingMessage("File %s could not be handled: %s", filename, LogUtils.toString(e)));
						LOG.error(String.format("Error on handling %s in %s: %s", entry, description(), LogUtils.toString(e)));
					}
				}
						
			}
			if (logMessage.length() != 0) {
				LOG.info(logMessage.toString());
			}
		} catch (final Exception e) {
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
