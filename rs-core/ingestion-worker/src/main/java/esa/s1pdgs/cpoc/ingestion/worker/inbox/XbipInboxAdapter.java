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

import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;
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
	public final InboxAdapterResponse read(final URI uri, final String name, final String relativePath, final long size) throws Exception {
		final XbipClient client = xbipClientFactory.newXbipClient(uri);		
		final Path basePath = IngestionJobs.basePath(uri, name);
		
		return new InboxAdapterResponse(
				// TODO: only list the content of the specified url
				client.list(XbipEntryFilter.ALLOW_ALL).stream()
				.map(x -> toInboxAdapterEntry(basePath, x, client.read(x)))
				.collect(Collectors.toList()), client);
	}

	@Override
	public final void delete(final URI uri) {
		// no deletiong for Xbip --> do nothing
	}

	@Override
	public final String toString() {
		return "XbipInboxAdapter";
	}
	
	private InboxAdapterEntry toInboxAdapterEntry(final Path parent, final XbipEntry entry, final InputStream in) {
		final Path thisPath = Paths.get(entry.getUri().getPath());		
		return new InboxAdapterEntry(parent.relativize(thisPath).toString(), in, entry.getSize());
	}
}
