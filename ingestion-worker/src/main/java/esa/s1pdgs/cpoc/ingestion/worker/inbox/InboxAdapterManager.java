package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.net.URI;
import java.util.Map;

public class InboxAdapterManager {
	private final Map<String,InboxAdapter> protocolToInboxAdapter;
	
	public InboxAdapterManager(final Map<String, InboxAdapter> protocolToInboxAdapter) {
		this.protocolToInboxAdapter = protocolToInboxAdapter;
	}

	public InboxAdapter getInboxAdapterFor(final URI uri) {
		final InboxAdapter result = protocolToInboxAdapter.get(uri.getScheme());
		if (result == null) {
			throw new IllegalArgumentException(
					String.format(
							"No InboxAdapter configured for protocol '%s' (uri: %s), available are %s",
							uri.getScheme(),
							uri,
							protocolToInboxAdapter
					)
			);
		}
		return result;		
	}
}
