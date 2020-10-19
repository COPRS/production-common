package esa.s1pdgs.cpoc.ebip.client.apacheftp;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import esa.s1pdgs.cpoc.ebip.client.EdipClient;
import esa.s1pdgs.cpoc.ebip.client.EdipClientFactory;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties;
import esa.s1pdgs.cpoc.ebip.client.config.EdipClientConfigurationProperties.EdipHostConfiguration;

@Component
public class ApacheFtpEdipClientFactory implements EdipClientFactory {
	private final EdipClientConfigurationProperties config;
	
	@Autowired
	public ApacheFtpEdipClientFactory(final EdipClientConfigurationProperties config) {
		this.config = config;
	}

	@Override
	public EdipClient newEdipClient(final URI serverUrl) {
		return new ApacheFtpEdipClient(configFor(serverUrl), serverUrl);
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
