package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;

public final class XbipInboxAdapter implements InboxAdapter {	
	private final XbipClientFactory xbipClientFactory;
	
	public XbipInboxAdapter(final XbipClientFactory xbipClientFactory) {
		this.xbipClientFactory = xbipClientFactory;
	}

	@Override
	public final List<InboxAdapterEntry> read(final URI uri, final String name) throws Exception {
		final XbipClient client = xbipClientFactory.newXbipClient(uri);		
		final Path basePath = Paths.get(uri.getPath());
		
		// only list the content of the specified url
		return client.list(XbipEntryFilter.ALLOW_ALL).stream()
			.map(x -> toInboxAdapterEntry(basePath.getParent(), x, client.read(x)))
			.collect(Collectors.toList());
	}

	@Override
	public final void delete(final URI uri) throws Exception {
		// no deletiong for Xbip --> do nothing
	}

	@Override
	public final String toString() {
		return "XbipInboxAdapter";
	}
	
	private final InboxAdapterEntry toInboxAdapterEntry(final Path parent, final XbipEntry entry, final InputStream in) {
		final Path thisPath = Paths.get(entry.getUri().getPath());		
		return new InboxAdapterEntry(parent.relativize(thisPath).toString(), in, entry.getSize());
	}
}
