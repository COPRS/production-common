package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public abstract class AbstractInboxAdapter implements InboxAdapter {
	public static class EntrySupplier {
		private final Path path;
		private final Supplier<InboxEntry> entry;
		
		public EntrySupplier(final Path path, final Supplier<InboxEntry> entry) {
			this.path = path;
			this.entry = entry;
		}

		public Path getPath() {
			return path;
		}

		public InboxEntry getEntry() {
			return entry.get();
		}
	}
	
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	
	protected final InboxEntryFactory inboxEntryFactory;
	protected final URI inboxURL;
	protected final String stationName;
	
	public AbstractInboxAdapter(
			final InboxEntryFactory inboxEntryFactory, 
			final URI inboxURL, 
			final String stationName
	) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.inboxURL = inboxURL;
		this.stationName = stationName;
	}
	
	protected abstract Stream<EntrySupplier> list() throws IOException;
	
	@Override
	public List<InboxEntry> read(final InboxFilter filter) throws IOException {
		LOG.trace("Reading inbox directory '{}'", inboxURL.toString());
		final List<InboxEntry> entries = list()
				.filter(x -> !Paths.get(inboxURL.getPath()).equals(x.getPath()))
				.map(x -> x.getEntry())
				.filter(e -> filter.accept(e))
				.collect(Collectors.toList());
		LOG.trace("Found {} entries in inbox directory '{}': {}", entries.size(), inboxURL.toString(), entries);
		return entries;
	}

	@Override
	public final String description() {
		return String.format("Inbox at %s", inboxURL.toString());
	}

	@Override
	public final String inboxURL() {
		return inboxURL.toString();
	}

	@Override
	public final String toString() {
		return String.format("%s [inboxDirectory=%s]", getClass().getSimpleName(), inboxURL.toString());
	}	

}
