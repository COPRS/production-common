package esa.s1pdgs.cpoc.ipf.preparation.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration of applicative data catalog client
 */
@Configuration
public class AppCatalogConfiguration {
	private final AppCatalogConfigurationProperties properties;
	private final RestTemplate restTemplate;

	@Autowired
	public AppCatalogConfiguration(
			final AppCatalogConfigurationProperties properties,
			final RestTemplateBuilder restTemplateBuilder
	) {
		this.properties = properties;
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(properties.getTmConnectMs())
				.build();
	}
	

}
