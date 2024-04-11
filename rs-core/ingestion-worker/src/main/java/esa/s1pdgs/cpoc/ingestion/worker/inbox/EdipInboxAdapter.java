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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.EdipEntry;
import esa.s1pdgs.cpoc.ebip.client.EdipEntryFilter;
import esa.s1pdgs.cpoc.ingestion.worker.product.IngestionJobs;

public final class EdipInboxAdapter implements InboxAdapter {	
	
	private static final Logger LOG = LoggerFactory.getLogger(EdipInboxAdapter.class);
	private final EdipClientFactory edipClientFactory;
	
	public EdipInboxAdapter(final EdipClientFactory edipClientFactory) {
		this.edipClientFactory = edipClientFactory;
	}

	@Override
	public final InboxAdapterResponse read(final URI uri, final String name, final String relativePath, final long size) throws Exception {
		final EdipClient client = edipClientFactory.newEdipClient(uri, false);	
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
