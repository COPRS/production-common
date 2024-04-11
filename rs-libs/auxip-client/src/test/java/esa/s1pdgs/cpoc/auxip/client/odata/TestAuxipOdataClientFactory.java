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

package esa.s1pdgs.cpoc.auxip.client.odata;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import esa.s1pdgs.cpoc.auxip.client.AuxipClient;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties;
import esa.s1pdgs.cpoc.auxip.client.config.AuxipClientConfigurationProperties.AuxipHostConfiguration;

public class TestAuxipOdataClientFactory {
	
	@Test
	public final void newAuxipClient_basic() throws URISyntaxException {
		
		AuxipHostConfiguration hostConfig = new AuxipHostConfiguration();
		hostConfig.setServiceRootUri("http://localhost/odata/v1/");
		hostConfig.setCreationDateAttributeName("creationDate");
		hostConfig.setProductNameAttrName("name");
		hostConfig.setIdAttrName("id");
		hostConfig.setContentLengthAttrName("contentLength");
		hostConfig.setAuthType("basic");
		hostConfig.setUser("user");
		AuxipClientConfigurationProperties properties = new AuxipClientConfigurationProperties();
		
		Map<String, AuxipHostConfiguration> configs = new HashMap<>();
		configs.put("host1", hostConfig);
		properties.setHostConfigs(configs);
		
		AuxipOdataClientFactory factory = new AuxipOdataClientFactory(properties);
		AuxipClient newAuxipClient = factory.newAuxipClient(new URI("http://localhost/odata/v1/"));
		assertNotNull(newAuxipClient);
	}
	
	@Test
	public final void newAuxipClient_oauth() throws URISyntaxException {
		
		AuxipHostConfiguration hostConfig = new AuxipHostConfiguration();
		hostConfig.setServiceRootUri("http://localhost/odata/v1/");
		hostConfig.setCreationDateAttributeName("creationDate");
		hostConfig.setProductNameAttrName("name");
		hostConfig.setIdAttrName("id");
		hostConfig.setContentLengthAttrName("contentLength");
		hostConfig.setAuthType("oauth2");
		hostConfig.setUser("user");
		hostConfig.setPass("pass");
		hostConfig.setOauthAuthUrl("http://localhost/odata/v1/");
		hostConfig.setOauthClientId("clientId");
		hostConfig.setOauthClientSecret("secret");
		
		AuxipClientConfigurationProperties properties = new AuxipClientConfigurationProperties();
		Map<String, AuxipHostConfiguration> configs = new HashMap<>();
		configs.put("host1", hostConfig);
		properties.setHostConfigs(configs);
		
		AuxipOdataClientFactory factory = new AuxipOdataClientFactory(properties);
		AuxipClient newAuxipClient = factory.newAuxipClient(new URI("http://localhost/odata/v1/"));
		assertNotNull(newAuxipClient);
	}

}
