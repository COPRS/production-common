package fr.viveris.s1pdgs.archives.services;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.viveris.s1pdgs.libs.obs_sdk.ObsClient;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsClientBuilder;
import fr.viveris.s1pdgs.libs.obs_sdk.ObsServiceException;

/**
 * Configuration for accessing to object storage with Amazon S3 API
 * 
 * @author Cyrielle Gailliard
 *
 */
@Configuration
public class ObsConfig {

	/**
	 * Amazon S3 client
	 * 
	 * @return
	 * @throws ObsServiceException 
	 */
	@Bean
	public ObsClient obsClient() throws ObsServiceException {
		return ObsClientBuilder.defaultClient();
	}
}
