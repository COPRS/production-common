package esa.s1pdgs.cpoc.compression.obs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsClientBuilder;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

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
