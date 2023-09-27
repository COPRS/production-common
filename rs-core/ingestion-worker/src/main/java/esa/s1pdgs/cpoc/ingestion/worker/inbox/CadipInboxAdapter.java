package esa.s1pdgs.cpoc.ingestion.worker.inbox;

import java.io.InputStream;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.CadipClientFactory;
import de.werum.coprs.cadip.client.model.CadipFile;

public class CadipInboxAdapter implements InboxAdapter {
	private final CadipClientFactory cadipClientFactory;	
	
	public CadipInboxAdapter(CadipClientFactory cadipClientFactory) {
		this.cadipClientFactory = cadipClientFactory;
	}
	
	@Override
	public InboxAdapterResponse read(URI uri, String name, String relativePath, long size) throws Exception {
		final CadipClient cadipClient = cadipClientFactory.newCadipClient(uri);
		List<CadipFile> cadipFiles = cadipClient.getFiles(null, name, null);
		if(cadipFiles.size() == 1) {
			InputStream in = cadipClient.downloadFile(cadipFiles.get(0).getId());
			return new InboxAdapterResponse(Collections.singletonList(new InboxAdapterEntry(relativePath, in, size)), cadipClient);
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
