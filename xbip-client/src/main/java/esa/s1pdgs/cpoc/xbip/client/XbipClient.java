package esa.s1pdgs.cpoc.xbip.client;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;

public interface XbipClient {	
	static final XbipClient NULL = new XbipClient() {
		@Override
		public final List<XbipEntry> list(final XbipEntryFilter filter) throws IOException {
			return Collections.emptyList();
		}		
	};
	
	static XbipClient newXbipClient(final URI serverUrl) {
		// TODO
		return NULL;
	}
	
	List<XbipEntry> list(XbipEntryFilter filter) throws IOException;
}
