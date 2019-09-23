package esa.s1pdgs.cpoc.inbox;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.inbox.entity.InboxEntry;
import esa.s1pdgs.cpoc.inbox.filter.InboxFilter;
import esa.s1pdgs.cpoc.inbox.kafka.producer.SubmissionClient;
import esa.s1pdgs.cpoc.mqi.model.queue.IngestionDto;

public final class Inbox {
	private static final Logger LOG = LoggerFactory.getLogger(Inbox.class);

	private final InboxAdapter inboxAdapter;
	private final List<InboxFilter> filter;
	private final InboxPollingServiceTransactional inboxPollingServiceTransactional;
	private final SubmissionClient client;
	private final String hostname;

	Inbox(final InboxAdapter inboxAdapter, final List<InboxFilter> filter,
			final InboxPollingServiceTransactional inboxPollingServiceTransactional, final SubmissionClient client, final String hostname) {
		this.inboxAdapter = inboxAdapter;
		this.filter = filter;
		this.inboxPollingServiceTransactional = inboxPollingServiceTransactional;
		this.client = client;
		this.hostname = hostname;
	}

	public final void poll() {
		try {
			final Set<InboxEntry> pickupContent = new HashSet<>(inboxAdapter.read(filter));
			final Set<InboxEntry> persistedContent = inboxPollingServiceTransactional
					.getAllForPath(inboxAdapter.inboxPath());

			final Set<InboxEntry> newElements = new HashSet<>(pickupContent);
			newElements.removeAll(persistedContent);

			final Set<InboxEntry> finishedElements = new HashSet<>(persistedContent);
			finishedElements.removeAll(pickupContent);

			if (finishedElements.size() != 0) {
				LOG.info("Got {} finished elements: {}", finishedElements.size(), finishedElements);
				// when a product has been removed from the inbox directory, it shall be removed
				// from the
				// persistence so it will not be ignored if it occurs again on the inbox
				LOG.debug("Deleting all {} from persistence", finishedElements);
				inboxPollingServiceTransactional.removeFinished(finishedElements);
			}

			if (newElements.size() != 0) {
				LOG.info("Got {} new elements: {}", newElements.size(), newElements);

				// all products not stored in the repo are considered new and shall be added to
				// the
				// configured queue.
				newElements.stream().forEach(e -> handleNew(e));
			}
		} catch (Exception e) {
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

	private void handleNew(final InboxEntry entry) {
		try {
			LOG.info("Publishing new entry to kafka queue: {}", entry);
			IngestionDto dto = new IngestionDto(entry.getName());
			dto.setHostname(hostname);
		    dto.setRelativePath(entry.getRelativePath());
		    dto.setPickupPath(entry.getPickupPath());
			dto.setMissionId(entry.getMissionId());
			dto.setSatelliteId(entry.getSatelliteId());
			dto.setStationCode(entry.getStationCode());
			client.publish(dto);
			final InboxEntry persisted = inboxPollingServiceTransactional.add(entry);
			LOG.debug("Added {} to persistence", persisted);
		} catch (Exception e) {
			LOG.error(String.format("Error on handling %s in %s", entry, description()), e);
		}
	}

}
