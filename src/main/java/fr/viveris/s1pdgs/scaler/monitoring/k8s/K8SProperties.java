package fr.viveris.s1pdgs.scaler.monitoring.k8s;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "k8s")
public class K8SProperties {
	
	private String masterUrl;
	
	private String username;
	
	private String clientKey;
	
	private String clientCertData;
	
	private String namespace;

	public K8SProperties() {
		
	}

	/**
	 * @return the masterUrl
	 */
	public String getMasterUrl() {
		return masterUrl;
	}

	/**
	 * @param masterUrl the masterUrl to set
	 */
	public void setMasterUrl(String masterUrl) {
		this.masterUrl = masterUrl;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the clientKey
	 */
	public String getClientKey() {
		return clientKey;
	}

	/**
	 * @param clientKey the clientKey to set
	 */
	public void setClientKey(String clientKey) {
		this.clientKey = clientKey;
	}

	/**
	 * @return the clientCertData
	 */
	public String getClientCertData() {
		return clientCertData;
	}

	/**
	 * @param clientCertData the clientCertData to set
	 */
	public void setClientCertData(String clientCertData) {
		this.clientCertData = clientCertData;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

}
