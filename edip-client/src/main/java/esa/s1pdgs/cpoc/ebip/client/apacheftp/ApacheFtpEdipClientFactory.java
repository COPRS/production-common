package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.net.URI;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

public class ApacheFtpEdipClientFactory implements EdipClientFactory {
	private final EdipClientConfigurationProperties config;

	@Override
	public EdipClient newEdipClient(final URI serverUrl) {
		return new Ap;
	}
	
	private final EdipHostConfiguration configFor(final URI serverUrl) {
		for (final EdipHostConfiguration hostConfig : config.getHostConfigs()) {
			if (hostConfig.getServerName().equals(serverUrl.getHost())) {
				return hostConfig;
			}
		}
		throw new IllegalArgumentException(
				String.format("Could not find configuration for '%s'", serverUrl)
		);
	}

}
