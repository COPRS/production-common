package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;

public class XbipInboxAdapter extends AbstractInboxAdapter {
	private final XbipClient xbipClient;
	
	public XbipInboxAdapter(
			final URI inboxURL, 
			final XbipClient xbipClient, 
			final XbipInboxEntryFactory inboxEntryFactory,
			final int productInDirectoryLevel,
			final String stationName
	) {
		super(inboxEntryFactory, inboxURL, productInDirectoryLevel, stationName);
		this.xbipClient = xbipClient;
	}
	
	@Override
	protected Stream<EntrySupplier> list(final InboxFilter filter) throws IOException {
		return xbipClient.list(XbipEntryFilter.ALLOW_ALL).stream()
				.map(p -> new EntrySupplier(p.getPath(), () -> newInboxEntryFor(p)));
	}

	private final InboxEntry newInboxEntryFor(final XbipEntry xbipEntry) {
		return inboxEntryFactory.newInboxEntry(
				inboxURL, 
				xbipEntry.getPath(), 
				productInDirectoryLevel,
				xbipEntry.getLastModified(), 
				xbipEntry.getSize(),
				stationName
		);
	}
}
