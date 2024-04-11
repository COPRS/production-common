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

package esa.s1pdgs.cpoc.ingestion.trigger.xbip;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.ingestion.trigger.entity.InboxEntry;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.AbstractInboxAdapter;
import esa.s1pdgs.cpoc.ingestion.trigger.inbox.InboxEntryFactory;
import esa.s1pdgs.cpoc.xbip.client.XbipClient;
import esa.s1pdgs.cpoc.xbip.client.XbipEntry;
import esa.s1pdgs.cpoc.xbip.client.XbipEntryFilter;

public class XbipInboxAdapter extends AbstractInboxAdapter {
	
	public final static String INBOX_TYPE = "xbip";
	
	private final XbipClient xbipClient;
	
	public XbipInboxAdapter(
			final URI inboxURL, 
			final XbipClient xbipClient, 
			final InboxEntryFactory inboxEntryFactory,
			final String stationName,
			final String missionId,
			final ProductFamily productFamily
	) {
		super(inboxEntryFactory, inboxURL, stationName, missionId, productFamily);
		this.xbipClient = xbipClient;
	}
	
	@Override
	protected Stream<EntrySupplier> list() throws IOException {
		return xbipClient.list(XbipEntryFilter.ALLOW_ALL).stream()
				.map(p -> new EntrySupplier(p.getPath(), () -> newInboxEntryFor(p)));
	}

	private final InboxEntry newInboxEntryFor(final XbipEntry xbipEntry) {
		return inboxEntryFactory.newInboxEntry(
				inboxURL, 
				xbipEntry.getPath(), 
				xbipEntry.getLastModified(), 
				xbipEntry.getSize(),
				stationName,
				missionId,
				INBOX_TYPE,
				productFamily
		);
	}
}
