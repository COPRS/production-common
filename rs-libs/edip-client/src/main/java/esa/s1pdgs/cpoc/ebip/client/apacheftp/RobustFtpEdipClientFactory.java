package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

@Component
public class RobustFtpEdipClientFactory implements EdipClientFactory {
	private final EdipClientConfigurationProperties config;
	
	@Autowired
	public RobustFtpEdipClientFactory(final EdipClientConfigurationProperties config) {
		this.config = config;
	}

	@Override
	public EdipClient newEdipClient(final URI serverUrl, final boolean directoryListing) {
		return new RobustFtpClient(configFor(serverUrl), serverUrl, directoryListing);
	}
	
	private final EdipHostConfiguration configFor(final URI serverUrl) {
		for (final EdipHostConfiguration hostConfig : config.getHostConfigs().values()) {
			if (hostConfig.getServerName().equals(serverUrl.getHost())) {
				return hostConfig;
			}
		}
		throw new IllegalArgumentException(
				String.format("Could not find configuration for '%s'", serverUrl)
		);
	}

}
