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
		List<CadipFile> cadipFiles = cadipClient.getFiles(null, name, null);
		if (cadipFiles.size() == 1) {
			CadipFile file = cadipFiles.get(0);
			InputStream in = cadipClient.downloadFile(file.getId());

			List<InboxAdapterEntry> entries = new ArrayList<>(
					Collections.singletonList(new InboxAdapterEntry(relativePath, in, size)));

			// If file is finalBlock write DSIB file
			if (file.getFinalBlock()) {
				LOG.info("Create DSIB for channel {} as this is the final file", file.getChannel());
				List<CadipFile> filesInSession = cadipClient.getFiles(file.getSessionId(), null, null);

				List<CadipSession> sessionsWithSessionId = cadipClient.getSessionsBySessionId(file.getSessionId());
				// We assume to receive a session here, as the file is related to at least one
				// session object
				CadipSession session = sessionsWithSessionId.get(0);

				String xmlContent = generateDSIB(file, filesInSession, session);

				entries.add(new InboxAdapterEntry(file.getSessionId() + "/" + DSIBXmlGenerator.generateName(file.getSessionId(), file.getChannel()),
						new ByteArrayInputStream(xmlContent.getBytes()), xmlContent.length()));
			}

			return new InboxAdapterResponse(entries, cadipClient);
		}
		return null;
	}

	@Override
	public void delete(URI uri) {
		// TODO Auto-generated method stub

	}

	@Override
	public final String toString() {
		return "CadipInboxAdapter";
	}

	private String generateDSIB(final CadipFile file, final List<CadipFile> filesInSession, final CadipSession session) {
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

		return DSIBXmlGenerator.generate(session.getSessionId(),
				filesInSession.stream().filter(f -> f.getChannel() == file.getChannel()).map(f -> f.getName())
						.collect(Collectors.toList()),
				start, stop,
				filesInSession.stream().reduce(0L, (sum, f) -> sum + f.getSize(), (sum1, sum2) -> sum1 + sum2));
	}

}
