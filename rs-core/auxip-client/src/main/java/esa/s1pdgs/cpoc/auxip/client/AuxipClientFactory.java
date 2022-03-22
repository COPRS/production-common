package esa.s1pdgs.cpoc.auxip.client;

import java.net.URI;

public interface AuxipClientFactory {
	
	AuxipClient newAuxipClient(URI serverUrl);

}
