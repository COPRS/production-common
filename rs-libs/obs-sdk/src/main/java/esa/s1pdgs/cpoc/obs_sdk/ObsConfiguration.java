/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.obs_sdk;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.obs_sdk.report.ReportingProductFactory;
import esa.s1pdgs.cpoc.obs_sdk.s3.S3ObsClient;

@Configuration
public class ObsConfiguration {	
	private final ObsConfigurationProperties config;
	private final ReportingProductFactory factory;
		
	@Autowired
	public ObsConfiguration(final ObsConfigurationProperties config, final ReportingProductFactory factory) {
		this.config = config;
		this.factory = factory;
	}
	
	@Bean
	public ObsClient newObsClient() {
		final ObsClient.Factory obsClientFactory = factoryForBackend(config.getBackend());
		return obsClientFactory.newObsClient(config, factory);		
	}
	
	public final ObsClient.Factory factoryForBackend(final String backend) {
		if (S3ObsClient.BACKEND_NAME.equals(backend)) {			
			return new S3ObsClient.Factory();
		}
		throw new IllegalArgumentException(
				String.format(
						"Invalid OBS backend %s. Allowed are: %s", 
						backend, 
						Arrays.asList(S3ObsClient.BACKEND_NAME)
				)
		);
	}
}
