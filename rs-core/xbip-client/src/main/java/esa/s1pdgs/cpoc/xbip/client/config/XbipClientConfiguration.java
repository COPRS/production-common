package esa.s1pdgs.cpoc.xbip.client.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.xbip.client.XbipClientFactory;
import esa.s1pdgs.cpoc.xbip.client.sardine.SardineXbipClientFactory;

@Configuration
public class XbipClientConfiguration {
	private final XbipClientConfigurationProperties config;
		
	@Autowired
	public XbipClientConfiguration(final XbipClientConfigurationProperties config) {
		this.config = config;
	}

	@Bean
	public XbipClientFactory xbipClientFactory() {
		return new SardineXbipClientFactory(config);
	}
}
