package fr.viveris.s1pdgs.scaler.openstack;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "openstack.server")
public class OpenStackServerProperties {
	
	private String endpoint;
	
	private String domainId;
	
	private String projectId;
	
	private String credentialUsername;
	
	private String credentialPassword;

	public OpenStackServerProperties() {
		
	}

	/**
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint the endpoint to set
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * @return the domainId
	 */
	public String getDomainId() {
		return domainId;
	}

	/**
	 * @param domainId the domainId to set
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	/**
	 * @return the projectId
	 */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * @return the credentialUsername
	 */
	public String getCredentialUsername() {
		return credentialUsername;
	}

	/**
	 * @param credentialUsername the credentialUsername to set
	 */
	public void setCredentialUsername(String credentialUsername) {
		this.credentialUsername = credentialUsername;
	}

	/**
	 * @return the credentialPassword
	 */
	public String getCredentialPassword() {
		return credentialPassword;
	}

	/**
	 * @param credentialPassword the credentialPassword to set
	 */
	public void setCredentialPassword(String credentialPassword) {
		this.credentialPassword = credentialPassword;
	}

}
