package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

@Component
public class InboxEntryFactoryImpl implements InboxEntryFactory {
	@Override
	public InboxEntry newInboxEntry(
			final URI inboxURL, 
			final Path path, 
			final Date lastModified, 
			final long size, 
			final String stationName
	) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = Paths.get(inboxURL.getPath()).relativize(path);
		inboxEntry.setName(relativePath.toString());
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupURL(inboxURL.toString());
		inboxEntry.setLastModified(lastModified);
		inboxEntry.setSize(size);
		inboxEntry.setStationName(stationName);
		return inboxEntry;
	}
}
