package esa.s1pdgs.cpoc.archives.services;

import org.springframework.beans.factory.annotation.Value;
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
	public ObsClient obsClient(@Value("${obs.protocol:S3}") final String obsProtocol) throws ObsServiceException {	
		return  ObsClientBuilder.defaultClient(obsProtocol);
	}
	
}
