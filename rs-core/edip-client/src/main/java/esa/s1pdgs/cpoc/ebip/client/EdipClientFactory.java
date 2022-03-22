package esa.s1pdgs.cpoc.ebip.client;

import java.net.URI;

public interface EdipClientFactory {
	EdipClient newEdipClient(URI serverUrl, boolean directoryListing);
}
