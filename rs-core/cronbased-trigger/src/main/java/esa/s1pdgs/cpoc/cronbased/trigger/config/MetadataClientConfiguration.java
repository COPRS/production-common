package esa.s1pdgs.cpoc.cronbased.trigger.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

@Configuration
public class MetadataClientConfiguration {

	@Autowired
	private MetadataClientProperties properties;

	@Bean
	public MetadataClient metadataClient(final RestTemplateBuilder builder) {
		return new MetadataClient(builder.build(), properties.getMetadataHostname(), properties.getNbretry(),
				properties.getTemporetryms());
	}

}
