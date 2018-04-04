package fr.viveris.s1pdgs.scaler.k8s;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "k8s")
public class K8SProperties {
	
	// ------------------------
	// Connection settings
	// ------------------------
	private String masterUrl;
	private String username;
	private String clientKey;
	private String clientCertData;
	private String namespace;
	
	// -------------------------
	// Wrapper configuration
	// -------------------------
	private LabelKubernetes labelWrapperConfig;
	private LabelKubernetes labelWrapperStateUsed;
	private LabelKubernetes labelWrapperStateUnused;
	private LabelKubernetes labelWrapperApp;

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

	/**
	 * @return the labelWrapperConfig
	 */
	public LabelKubernetes getLabelWrapperConfig() {
		return labelWrapperConfig;
	}

	/**
	 * @param labelWrapperConfig the labelWrapperConfig to set
	 */
	public void setLabelWrapperConfig(LabelKubernetes labelWrapperConfig) {
		this.labelWrapperConfig = labelWrapperConfig;
	}

	/**
	 * @return the labelWrapperStateUsed
	 */
	public LabelKubernetes getLabelWrapperStateUsed() {
		return labelWrapperStateUsed;
	}

	/**
	 * @param labelWrapperStateUsed the labelWrapperStateUsed to set
	 */
	public void setLabelWrapperStateUsed(LabelKubernetes labelWrapperStateUsed) {
		this.labelWrapperStateUsed = labelWrapperStateUsed;
	}

	/**
	 * @return the labelWrapperStateUnused
	 */
	public LabelKubernetes getLabelWrapperStateUnused() {
		return labelWrapperStateUnused;
	}

	/**
	 * @param labelWrapperStateUnused the labelWrapperStateUnused to set
	 */
	public void setLabelWrapperStateUnused(LabelKubernetes labelWrapperStateUnused) {
		this.labelWrapperStateUnused = labelWrapperStateUnused;
	}

	/**
	 * @return the labelWrapperApp
	 */
	public LabelKubernetes getLabelWrapperApp() {
		return labelWrapperApp;
	}

	/**
	 * @param labelWrapperApp the labelWrapperApp to set
	 */
	public void setLabelWrapperApp(LabelKubernetes labelWrapperApp) {
		this.labelWrapperApp = labelWrapperApp;
	}

	public static class LabelKubernetes {
		private String label;
		private String value;
		public LabelKubernetes() {
			
		}
		public LabelKubernetes(String label, String value) {
			this.label = label;
			this.value = value;
		}
		/**
		 * @return the label
		 */
		public String getLabel() {
			return label;
		}
		/**
		 * @param label the label to set
		 */
		public void setLabel(String label) {
			this.label = label;
		}
		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
		
	}
}
