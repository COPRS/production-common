package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;

public final class EdipInboxAdapter implements InboxAdapter {	
	private final EdipClientFactory edipClientFactory;
	
	public EdipInboxAdapter(final EdipClientFactory edipClientFactory) {
		this.edipClientFactory = edipClientFactory;
	}

	@Override
	public final InboxAdapterResponse read(final URI uri, final String name, final String relativePath, final long size) throws Exception {
		final EdipClient client = edipClientFactory.newEdipClient(uri);	
		final Path basePath = IngestionJobs.basePath(uri, name);
		
		return new InboxAdapterResponse(
				// TODO: only list the content of the specified url
				client.list(EdipEntryFilter.ALLOW_ALL).stream()
				.map(x -> toInboxAdapterEntry(basePath, x, client.read(x)))
				.collect(Collectors.toList()), client);
	}

	@Override
	public final void delete(final URI uri) {
		// no deletiong for Edip --> do nothing
	}

	@Override
	public final String toString() {
		return "EdipInboxAdapter";
	}
	
	private final InboxAdapterEntry toInboxAdapterEntry(final Path parent, final EdipEntry entry, final InputStream in) {
		final Path thisPath = Paths.get(entry.getUri().getPath());		
		return new InboxAdapterEntry(parent.relativize(thisPath).toString(), in, entry.getSize());
	}
}
