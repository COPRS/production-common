/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
		return "file:/.*";
	}

	public static String uriRegexFor(String hostName) {
		return String.format(".*%s.*", hostName.replaceAll("[${}]", ""));
	}
}
