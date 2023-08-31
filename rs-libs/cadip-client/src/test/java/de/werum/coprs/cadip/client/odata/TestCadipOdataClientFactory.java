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
