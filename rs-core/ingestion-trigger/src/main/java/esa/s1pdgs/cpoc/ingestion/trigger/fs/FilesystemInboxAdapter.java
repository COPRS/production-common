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

package esa.s1pdgs.cpoc.ingestion.trigger.fs;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.SupportsProductFamily;

public class FilesystemInboxAdapter extends AbstractInboxAdapter implements SupportsProductFamily {
	public FilesystemInboxAdapter(
			final InboxEntryFactory inboxEntryFactory, 
			final URI inboxURL, 	
			final String stationName,
			final String missionId,
			final ProductFamily productFamily
	) {
		super(inboxEntryFactory, inboxURL, stationName, missionId, productFamily);
	}
	
	@Override
	protected Stream<EntrySupplier> list() throws IOException {
		return Files.walk(Paths.get(inboxURL.getPath()), FileVisitOption.FOLLOW_LINKS)
				.map(p -> new EntrySupplier(p, () -> newInboxEntryFor(p)));
	}
	
	private final InboxEntry newInboxEntryFor(final Path path) {		
		final File file = path.toFile();
		final Date lastModified = new Date(file.lastModified());
		final long size = FileUtils.sizeOf(file);		
		return inboxEntryFactory.newInboxEntry(
				inboxURL, 
				path, 
				lastModified, 
				size,
				stationName,
				missionId,
				"file",
				productFamily
		);
	}
}
