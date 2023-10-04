package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.CadipClientFactory;
import de.werum.coprs.cadip.client.model.CadipFile;
import de.werum.coprs.cadip.client.xml.DSIBXmlGenerator;

public class CadipInboxAdapter implements InboxAdapter {
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
				List<CadipFile> filesInSession = cadipClient.getFiles(file.getSessionId(), null, null);
				String xmlContent = DSIBXmlGenerator.generate(relativePath,
						filesInSession.stream().filter(f -> f.getChannel() == file.getChannel()).map(f -> f.getName())
								.collect(Collectors.toList()));

				entries.add(new InboxAdapterEntry(DSIBXmlGenerator.generateName(file.getSessionId(), file.getChannel()),
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
	
}
