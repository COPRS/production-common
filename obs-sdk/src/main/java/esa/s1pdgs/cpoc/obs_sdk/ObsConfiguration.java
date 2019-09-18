package esa.s1pdgs.cpoc.obs_sdk;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.swift.SwiftObsClient;

@Configuration
public class ObsConfiguration {	
	private final ObsConfigurationProperties config;
		
	@Autowired
	public ObsConfiguration(ObsConfigurationProperties config) {
		this.config = config;
	}
	
	@Bean
	public ObsClient newObsClient() {
		final ObsClient.Factory obsClientFactory = factoryForBackend(config.getBackend());
		return obsClientFactory.newObsClient(config);		
	}
	
	public final ObsClient.Factory factoryForBackend(final String backend) {
		if (S3ObsClient.BACKEND_NAME.equals(backend)) {			
			return new S3ObsClient.Factory();
		}
		if (SwiftObsClient.BACKEND_NAME.equals(backend)) {			
			return new S3ObsClient.Factory();
		}
		throw new IllegalArgumentException(
				String.format(
						"Invalid OBS backend %s. Allowed are: %s", 
						backend, 
						Arrays.asList(S3ObsClient.BACKEND_NAME, SwiftObsClient.BACKEND_NAME)
				)
		);
	}
}
