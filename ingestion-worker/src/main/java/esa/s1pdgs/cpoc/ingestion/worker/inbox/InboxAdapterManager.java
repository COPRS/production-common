package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

public class InboxAdapterManager {
	private final Map<String,InboxAdapter> uriRegexToInboxAdapter;
	
	public InboxAdapterManager(final Map<String, InboxAdapter> uriRegexToInboxAdapter) {
		this.uriRegexToInboxAdapter = uriRegexToInboxAdapter;
	}

	public InboxAdapter getInboxAdapterFor(final URI uri) {

		final String uriString = withProtocol(uri);

		Optional<InboxAdapter> result = uriRegexToInboxAdapter.entrySet().stream()
				.filter(entry -> uriString.matches(entry.getKey())).map(Map.Entry::getValue).findAny();

		return result.orElseThrow(() -> new IllegalArgumentException(
				String.format(
						"No InboxAdapter configured for uri: %s, available are %s",
						uri.getScheme(),
						uri,
						uriRegexToInboxAdapter
				)
		));
	}

	private String withProtocol(URI uri) {
		if(uri.getScheme() != null) {
			return uri.toString();
		}

		//if uri comes without scheme than add https:// as default
		return "https://" + uri.toString();
	}

	public static String uriRegexForFile() {
		return "file://.*";
	}

	public static String uriRegexFor(String hostName) {
		return String.format(".*://%s.*", hostName.replaceAll("[${}]", ""));
	}
}
