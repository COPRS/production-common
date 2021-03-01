package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import esa.s1pdgs.cpoc.common.utils.CollectionUtil;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

final class PollingRun {
	// just make sure top level dirs are handled first
	static final Comparator<InboxEntry> COMP = new Comparator<InboxEntry>() {
		@Override
		public final int compare(final InboxEntry o1, final InboxEntry o2) {			
			return Paths.get(o1.getRelativePath()).getNameCount() - Paths.get(o2.getRelativePath()).getNameCount();
		}		
	};
	
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
	
	static PollingRun newInstance(final Set<InboxEntry> persistedContent, final List<InboxEntry> pickupContent,
			final int stationRetentionTime) {
		// determine the entries that have been deleted from inbox to remove them from persistence
		final LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC)
				.minusDays(0 <= stationRetentionTime ? stationRetentionTime : 0);
		// I. make sure to keep entries persisted for at least [stationRetentionTime] days
		final Set<InboxEntry> finishedElements = CollectionUtil.nullToEmpty(persistedContent).stream()
				.filter(entry -> threshold.isAfter(entry.getKnownSince())).collect(Collectors.toSet());
		// II. then, if they aren't on the pickup anymore, consider them finished (= delete them)
		finishedElements.removeAll(pickupContent);

		// detect all elements that are considered "new" on the inbox
		final List<InboxEntry> newElements = new ArrayList<>(pickupContent);
		newElements.removeAll(persistedContent);
		Collections.sort(newElements, COMP);

		return new PollingRun(persistedContent, new HashSet<>(pickupContent), finishedElements, newElements);
	}

	static PollingRun newInstanceWithoutProductFamily(final Set<InboxEntry> persistedContent, final List<InboxEntry> pickupContent,
			final int stationRetentionTime) {
		// omitting product family comparison S1PRO-2395
		
		// determine the entries that have been deleted from inbox to remove them from persistence
		final LocalDateTime threshold = LocalDateTime.now(ZoneOffset.UTC)
				.minusDays(0 <= stationRetentionTime ? stationRetentionTime : 0);
		// I. make sure to keep entries persisted for at least [stationRetentionTime] days
		final Set<InboxEntry> oldElements = CollectionUtil.nullToEmpty(persistedContent).stream()
				.filter(entry -> threshold.isAfter(entry.getKnownSince())).collect(Collectors.toSet());
		// II. then, if they aren't on the pickup anymore, consider them finished (= delete them)
		final Set<InboxEntry> finishedElements = new HashSet<>(
				subtractWithoutProductFamily(oldElements, pickupContent));

		// detect all elements that are considered "new" on the inbox
		final List<InboxEntry> newElements = subtractWithoutProductFamily(pickupContent, persistedContent);
		Collections.sort(newElements, COMP);

		return new PollingRun(
				persistedContent,
				new HashSet<>(pickupContent),
				finishedElements,
				newElements
				);
	}

	private static List<InboxEntry> subtractWithoutProductFamily(final Collection<InboxEntry> minuend, final Collection<InboxEntry> subtrahend) {
		// omitting product family comparison S1PRO-2395
		final List<InboxEntry> result = new ArrayList<>(CollectionUtil.nullToEmpty(minuend).size());

		for (final InboxEntry inboxEntry : CollectionUtil.nullToEmpty(minuend)) {
			if (!containsWithoutProductFamily(subtrahend, inboxEntry)) {
				result.add(inboxEntry);
			}
		}

		return result;
	}
	
	private static boolean containsWithoutProductFamily(final Collection<InboxEntry> collection, final InboxEntry inboxEntry) {
		// omitting product family comparison S1PRO-2395
		for (final InboxEntry elem : CollectionUtil.nullToEmpty(collection)) {
			if (inboxEntry.equalsWithoutProductFamily(elem)) {
				return true;
			}
		}

		return false;
	}

	public Set<InboxEntry> finishedElements() {
		return this.finishedElements;
	}

	public List<InboxEntry> newElements() {
		return this.newElements;
	}

	final void dumpTo(final Set<InboxEntry> handledElements, final Logger log) {
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