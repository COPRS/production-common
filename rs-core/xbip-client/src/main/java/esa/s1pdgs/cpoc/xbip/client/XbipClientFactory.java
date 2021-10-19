package esa.s1pdgs.cpoc.xbip.client;

import java.net.URI;

public interface XbipClientFactory {
	XbipClient newXbipClient(URI serverUrl);
}
