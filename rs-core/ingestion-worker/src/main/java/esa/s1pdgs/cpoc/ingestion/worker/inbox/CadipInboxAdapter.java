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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.CadipClientFactory;
import de.werum.coprs.cadip.client.model.CadipFile;
import de.werum.coprs.cadip.client.model.CadipSession;
import de.werum.coprs.cadip.client.xml.DSIBXmlGenerator;
import esa.s1pdgs.cpoc.common.utils.DateUtils;

public class CadipInboxAdapter implements InboxAdapter {
	private static final Logger LOG = LoggerFactory.getLogger(CadipInboxAdapter.class);
	private final CadipClientFactory cadipClientFactory;

	public CadipInboxAdapter(CadipClientFactory cadipClientFactory) {
		this.cadipClientFactory = cadipClientFactory;
	}

	@Override
	public InboxAdapterResponse read(URI uri, String name, String relativePath, long size) throws Exception {
		final CadipClient cadipClient = cadipClientFactory.newCadipClient(uri);
		CadipFile file = cadipClient.getFileById(name);
		InputStream in = cadipClient.downloadFile(file.getId());

		List<InboxAdapterEntry> entries = new ArrayList<>(
				Collections.singletonList(new InboxAdapterEntry(relativePath, in, size)));

		// If file is finalBlock write DSIB file
		if (file.getFinalBlock()) {
			LOG.info("Create DSIB for channel {} as this is the final file", file.getChannel());
			List<CadipFile> filesInSession = cadipClient.getFiles(file.getSessionId(), null, file.getRetransfer(),
					null);

			List<CadipSession> applicableSessions = cadipClient
					.getSessionsBySessionIdAndRetransfer(file.getSessionId(), file.getRetransfer());
			// We assume to receive a session here, as the file is related to at least one
			// session object
			CadipSession session = applicableSessions.get(0);

			String xmlContent = generateDSIB(file, filesInSession, session);

			entries.add(new InboxAdapterEntry(
					file.getSessionId() + "/" + DSIBXmlGenerator.generateName(file.getSessionId(), file.getChannel()),
					new ByteArrayInputStream(xmlContent.getBytes()), xmlContent.length()));
		}

		return new InboxAdapterResponse(entries, cadipClient);
	}

	@Override
	public void delete(URI uri) {
		// TODO Auto-generated method stub

	}

	@Override
	public final String toString() {
		return "CadipInboxAdapter";
	}

	private String generateDSIB(final CadipFile file, final List<CadipFile> filesInSession,
			final CadipSession session) {
		LocalDateTime startTime = session.getDownlinkStart();
		LocalDateTime stopTime = session.getDownlinkStop();
		String start = DateUtils.convertToMetadataDateTimeFormat(file.getSessionId().substring(4, 18));
		String stop = DateUtils.convertToMetadataDateTimeFormat(file.getSessionId().substring(4, 18));

		if (startTime != null) {
			start = DateUtils.formatToMetadataDateTimeFormat(startTime);
		}

		if (stopTime != null) {
			stop = DateUtils.formatToMetadataDateTimeFormat(stopTime);
		}

		return DSIBXmlGenerator.generate(file.getSessionId(),
				filesInSession.stream().filter(f -> f.getChannel() == file.getChannel()).map(f -> f.getName())
						.collect(Collectors.toList()),
				start, stop,
				filesInSession.stream().reduce(0L, (sum, f) -> sum + f.getSize(), (sum1, sum2) -> sum1 + sum2));
	}

}
