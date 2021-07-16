package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.net.URI;

public interface InboxAdapter {
	InboxAdapterResponse read(final URI uri, final String name, final String relativePath, final long size) throws Exception;
	void delete(final URI uri);
}
