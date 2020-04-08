package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.net.URI;
import java.util.List;

public interface InboxAdapter {
	List<InboxAdapterEntry> read(final URI uri, final String name) throws Exception;	
	void delete(final URI uri) throws Exception;
}
