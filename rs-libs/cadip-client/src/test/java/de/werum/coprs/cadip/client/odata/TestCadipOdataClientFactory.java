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

package de.werum.coprs.cadip.client.odata;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import de.werum.coprs.cadip.client.CadipClient;
import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties;
import de.werum.coprs.cadip.client.config.CadipClientConfigurationProperties.CadipHostConfiguration;

public class TestCadipOdataClientFactory {
	
	@Test
	public final void newCadipClient_basic() throws URISyntaxException {
		
		CadipHostConfiguration hostConfig = new CadipHostConfiguration();
		hostConfig.setServiceRootUri("http://localhost/odata/v1/");
		hostConfig.setAuthType("basic");
		hostConfig.setUser("user");
		CadipClientConfigurationProperties properties = new CadipClientConfigurationProperties();
		
		Map<String, CadipHostConfiguration> configs = new HashMap<>();
		configs.put("host1", hostConfig);
		properties.setHostConfigs(configs);
		
		CadipOdataClientFactory factory = new CadipOdataClientFactory(properties);
		CadipClient newCadipClient = factory.newCadipClient(new URI("http://localhost/odata/v1/"));
		assertNotNull(newCadipClient);
	}
	
	@Test
	public final void newCadipClient_oauth() throws URISyntaxException {
		
		CadipHostConfiguration hostConfig = new CadipHostConfiguration();
		hostConfig.setServiceRootUri("http://localhost/odata/v1/");
		hostConfig.setAuthType("oauth2");
		hostConfig.setUser("user");
		hostConfig.setPass("pass");
		hostConfig.setOauthAuthUrl("http://localhost/odata/v1/");
		hostConfig.setOauthClientId("clientId");
		hostConfig.setOauthClientSecret("secret");
		
		CadipClientConfigurationProperties properties = new CadipClientConfigurationProperties();
		Map<String, CadipHostConfiguration> configs = new HashMap<>();
		configs.put("host1", hostConfig);
		properties.setHostConfigs(configs);
		
		CadipOdataClientFactory factory = new CadipOdataClientFactory(properties);
		CadipClient newCadipClient = factory.newCadipClient(new URI("http://localhost/odata/v1/"));
		assertNotNull(newCadipClient);
	}

}
