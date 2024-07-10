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

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.config.ProcessConfiguration;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;

@Component
public class InboxEntryFactoryImpl implements InboxEntryFactory {

	private final ProcessConfiguration processConfiguration;

	@Autowired
	public InboxEntryFactoryImpl(ProcessConfiguration processConfiguration) {
		this.processConfiguration = processConfiguration;
	}

	@Override
	public InboxEntry newInboxEntry(
			final URI inboxURL, 
			final Path path, 
			final Date lastModified, 
			final long size, 
			final String stationName,
			final String missionId,
			final String inboxType,
			final ProductFamily productFamily
	) {
		final InboxEntry inboxEntry = new InboxEntry();
		final Path relativePath = Paths.get(inboxURL.getPath()).relativize(path);

		inboxEntry.setName(relativePath.toString());
		inboxEntry.setRelativePath(relativePath.toString());
		inboxEntry.setPickupURL(inboxURL.toString());
		inboxEntry.setLastModified(lastModified);
		inboxEntry.setSize(size);
		inboxEntry.setStationName(stationName);
		inboxEntry.setMissionId(missionId);
		inboxEntry.setProcessingPod(processConfiguration.getHostname());
		inboxEntry.setInboxType(inboxType);
		inboxEntry.setProductFamily(productFamily.name());
		return inboxEntry;
	}
}
