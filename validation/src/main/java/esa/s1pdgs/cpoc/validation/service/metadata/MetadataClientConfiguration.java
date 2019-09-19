package esa.s1pdgs.cpoc.validation.service.metadata;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.metadata.client.MetadataClient;

@Configuration
public class MetadataClientConfiguration {

	/**
	 * Host URI for the applicative catalog server
	 */
	private final String metadataHostname;

	/**
	 * Maximal number of retries when query fails
	 */
	private final int nbretry;

	/**
	 * Temporisation in ms between 2 retries
	 */
	private final int temporetryms;

	@Autowired
	public MetadataClientConfiguration(@Value("${metadata.host}") final String metadataHostname,
			@Value("${metadata.rest-api_nb-retry}") final int nbretry,
			@Value("${metadata.rest-api_tempo-retry-ms}") final int temporetryms) {

		this.metadataHostname = metadataHostname;
		this.nbretry = nbretry;
		this.temporetryms = temporetryms;
	}
	
	@Bean
	public MetadataClient metadataClient(final RestTemplateBuilder builder) {
		return new MetadataClient(builder.build(), this.metadataHostname, this.nbretry, this.temporetryms);
	}

}
