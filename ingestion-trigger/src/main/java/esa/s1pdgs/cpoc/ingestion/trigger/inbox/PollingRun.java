package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;

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
	
	static PollingRun newInstance(final Set<InboxEntry> persistedContent, final List<InboxEntry> pickupContent) {
		// determine the entries that have been deleted from inbox to remove them from persistence
		final Set<InboxEntry> finishedElements = new HashSet<>(persistedContent);
		finishedElements.removeAll(pickupContent);
		
		// detect all elements that are considered "new" on the inbox
		final List<InboxEntry> newElements = new ArrayList<>(pickupContent);
		newElements.removeAll(persistedContent);
		Collections.sort(newElements, COMP);
		
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