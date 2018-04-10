package fr.viveris.s1pdgs.scaler.openstack.services;

import org.openstack4j.api.OSClient.OSClientV3;
import org.openstack4j.model.common.Identifier;
import org.openstack4j.model.identity.v3.Token;
import org.openstack4j.openstack.OSFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.viveris.s1pdgs.scaler.openstack.OpenStackServerProperties;

@Configuration
public class OpenStackConfig {

	private final OpenStackServerProperties properties;

	@Autowired
	public OpenStackConfig(final OpenStackServerProperties properties) {
		this.properties = properties;
	}

	@Bean
	public OSClientV3 osClient() {
		OSClientV3 os = OSFactory.builderV3().endpoint(properties.getEndpoint())
				.credentials(properties.getCredentialUsername(), properties.getCredentialPassword(),
						Identifier.byId(properties.getDomainId()))
				.scopeToProject(Identifier.byId(properties.getProjectId())).authenticate();
		return os;
	}
	
	@Bean
	public Token osToken() {
		OSClientV3 os = OSFactory.builderV3().endpoint(properties.getEndpoint())
				.credentials(properties.getCredentialUsername(), properties.getCredentialPassword(),
						Identifier.byId(properties.getDomainId()))
				.scopeToProject(Identifier.byId(properties.getProjectId())).authenticate();
		return os.getToken();
	}

}
