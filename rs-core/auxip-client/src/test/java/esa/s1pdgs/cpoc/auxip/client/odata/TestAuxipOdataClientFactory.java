package esa.s1pdgs.cpoc.auxip.client.odata;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

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
		properties.setHostConfigs(Arrays.asList(hostConfig));
		
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
		properties.setHostConfigs(Arrays.asList(hostConfig));
		
		AuxipOdataClientFactory factory = new AuxipOdataClientFactory(properties);
		AuxipClient newAuxipClient = factory.newAuxipClient(new URI("http://localhost/odata/v1/"));
		assertNotNull(newAuxipClient);
	}

}
