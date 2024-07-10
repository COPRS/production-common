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

package esa.s1pdgs.cpoc.ingestion.trigger.inbox;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.filter.InboxFilter;

public abstract class AbstractInboxAdapter implements InboxAdapter {
	public static class EntrySupplier {
		private final Path path;
		private final Supplier<InboxEntry> entry;
		
		public EntrySupplier(final Path path, final Supplier<InboxEntry> entry) {
			this.path = path;
			this.entry = entry;
		}

		public Path getPath() {
			return path;
		}

		public InboxEntry getEntry() {
			return entry.get();
		}
	}
	
	protected final Logger LOG = LoggerFactory.getLogger(getClass());
	
	protected final InboxEntryFactory inboxEntryFactory;
	protected final URI inboxURL;
	protected final String stationName;
	protected final String missionId;
	protected final ProductFamily productFamily;
	
	public AbstractInboxAdapter(
			final InboxEntryFactory inboxEntryFactory, 
			final URI inboxURL, 
			final String stationName,
			final String missionId,
			final ProductFamily productFamily
	) {
		this.inboxEntryFactory = inboxEntryFactory;
		this.inboxURL = inboxURL;
		this.stationName = stationName;
		this.missionId = missionId;
		this.productFamily = productFamily;
	}
	
	protected abstract Stream<EntrySupplier> list() throws IOException;
	
	@Override
	public List<InboxEntry> read(final InboxFilter filter) throws IOException {
		LOG.debug("Reading inbox directory '{}'", inboxURL.toString());
		final List<InboxEntry> entries = list()
				.filter(x -> !Paths.get(inboxURL.getPath()).equals(x.getPath()))
				.map(EntrySupplier::getEntry)
				.filter(filter::accept)
				.collect(Collectors.toList());
		LOG.debug("Found {} entries in inbox directory '{}': {}", entries.size(), inboxURL.toString(), entries);
		return entries;
	}

	@Override
	public void advanceAfterPublish() {
		//do nothing as default
	}

	@Override
	public final String description() {
		return String.format("Inbox at %s", inboxURL.toString());
	}

	@Override
	public final String inboxURL() {
		return inboxURL.toString();
	}

	@Override
	public final String toString() {
		return String.format("%s [inboxDirectory=%s]", getClass().getSimpleName(), inboxURL.toString());
	}	

}
