package de.werum.coprs.cadip.client;

import java.net.URI;

public interface CadipClientFactory {
	
	CadipClient newCadipClient(URI serverUrl);
}
