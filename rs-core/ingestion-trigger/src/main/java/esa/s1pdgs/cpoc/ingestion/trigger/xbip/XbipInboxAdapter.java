package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;

public class XbipInboxAdapter extends AbstractInboxAdapter {
	
	public final static String INBOX_TYPE = "xbip";
	
	private final XbipClient xbipClient;
	
	public XbipInboxAdapter(
			final URI inboxURL, 
			final XbipClient xbipClient, 
			final InboxEntryFactory inboxEntryFactory,
			final String stationName,
			final ProductFamily productFamily
	) {
		super(inboxEntryFactory, inboxURL, stationName, productFamily);
		this.xbipClient = xbipClient;
	}
	
	@Override
	protected Stream<EntrySupplier> list() throws IOException {
		return xbipClient.list(XbipEntryFilter.ALLOW_ALL).stream()
				.map(p -> new EntrySupplier(p.getPath(), () -> newInboxEntryFor(p)));
	}

	private final InboxEntry newInboxEntryFor(final XbipEntry xbipEntry) {
		return inboxEntryFactory.newInboxEntry(
				inboxURL, 
				xbipEntry.getPath(), 
				xbipEntry.getLastModified(), 
				xbipEntry.getSize(),
				stationName,
				INBOX_TYPE,
				productFamily
		);
	}
}
