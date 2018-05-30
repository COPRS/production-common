package fr.viveris.s1pdgs.jobgenerator.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for accessing to metadata
 * @author Cyrielle Gailliard
 *
 */
@Configuration
public class MetadataConfig {
	
	/**
	 * Template used for querying REST around metadata
	 * @param builder
	 * @return
	 */
	@Bean(name = "restMetadataTemplate")
	public RestTemplate restMetadataTemplate(final RestTemplateBuilder builder) {
		return builder.build();
	}

}
