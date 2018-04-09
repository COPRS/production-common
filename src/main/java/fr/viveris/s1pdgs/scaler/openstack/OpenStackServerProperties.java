package fr.viveris.s1pdgs.scaler.openstack;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "openstack.server")
public class OpenStackServerProperties {

	// -------------------------
	// Authentication
	// -------------------------
	private String endpoint;
	private String domainId;
	private String projectId;
	private String credentialUsername;
	private String credentialPassword;

	// -------------------------
	// Volumes
	// -------------------------
	private VolumeProperties volumeWrapper;
	private ServerProperties serverWrapper;

	public OpenStackServerProperties() {

	}

	/**
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint
	 *            the endpoint to set
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
	 * @param domainId
	 *            the domainId to set
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
	 * @param projectId
	 *            the projectId to set
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
	 * @param credentialUsername
	 *            the credentialUsername to set
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
	 * @param credentialPassword
	 *            the credentialPassword to set
	 */
	public void setCredentialPassword(String credentialPassword) {
		this.credentialPassword = credentialPassword;
	}

	/**
	 * @return the volumeWrapper
	 */
	public VolumeProperties getVolumeWrapper() {
		return volumeWrapper;
	}

	/**
	 * @param volumeWrapper
	 *            the volumeWrapper to set
	 */
	public void setVolumeWrapper(VolumeProperties volumeWrapper) {
		this.volumeWrapper = volumeWrapper;
	}

	/**
	 * @return the serverWrapper
	 */
	public ServerProperties getServerWrapper() {
		return serverWrapper;
	}

	/**
	 * @param serverWrapper the serverWrapper to set
	 */
	public void setServerWrapper(ServerProperties serverWrapper) {
		this.serverWrapper = serverWrapper;
	}

	public static class VolumeProperties {
		private String prefixName;
		private String description;
		private String imageRef;
		private String volumeType;
		private String zone;
		private int size;

		public VolumeProperties() {

		}

		/**
		 * @return the prefixName
		 */
		public String getPrefixName() {
			return prefixName;
		}

		/**
		 * @param prefixName
		 *            the prefixName to set
		 */
		public void setPrefixName(String prefixName) {
			this.prefixName = prefixName;
		}

		/**
		 * @return the description
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * @param description
		 *            the description to set
		 */
		public void setDescription(String description) {
			this.description = description;
		}

		/**
		 * @return the imageRef
		 */
		public String getImageRef() {
			return imageRef;
		}

		/**
		 * @param imageRef
		 *            the imageRef to set
		 */
		public void setImageRef(String imageRef) {
			this.imageRef = imageRef;
		}

		/**
		 * @return the volumeType
		 */
		public String getVolumeType() {
			return volumeType;
		}

		/**
		 * @param volumeType
		 *            the volumeType to set
		 */
		public void setVolumeType(String volumeType) {
			this.volumeType = volumeType;
		}

		/**
		 * @return the zone
		 */
		public String getZone() {
			return zone;
		}

		/**
		 * @param zone
		 *            the zone to set
		 */
		public void setZone(String zone) {
			this.zone = zone;
		}

		/**
		 * @return the size
		 */
		public int getSize() {
			return size;
		}

		/**
		 * @param size
		 *            the size to set
		 */
		public void setSize(int size) {
			this.size = size;
		}

	}

	public static class ServerProperties {
		private String prefixName;
		private String bootDeviceName;
		private String flavor;
		private String keySecurity;
		private String securityGroup;
		private List<String> networks;
		private String availableZone;
		private String floatingNetwork;

		public ServerProperties() {

		}

		/**
		 * @return the prefixName
		 */
		public String getPrefixName() {
			return prefixName;
		}

		/**
		 * @param prefixName
		 *            the prefixName to set
		 */
		public void setPrefixName(String prefixName) {
			this.prefixName = prefixName;
		}

		/**
		 * @return the bootDeviceName
		 */
		public String getBootDeviceName() {
			return bootDeviceName;
		}

		/**
		 * @param bootDeviceName
		 *            the bootDeviceName to set
		 */
		public void setBootDeviceName(String bootDeviceName) {
			this.bootDeviceName = bootDeviceName;
		}

		/**
		 * @return the flavor
		 */
		public String getFlavor() {
			return flavor;
		}

		/**
		 * @param flavor
		 *            the flavor to set
		 */
		public void setFlavor(String flavor) {
			this.flavor = flavor;
		}

		/**
		 * @return the keySecurity
		 */
		public String getKeySecurity() {
			return keySecurity;
		}

		/**
		 * @param keySecurity
		 *            the keySecurity to set
		 */
		public void setKeySecurity(String keySecurity) {
			this.keySecurity = keySecurity;
		}

		/**
		 * @return the securityGroup
		 */
		public String getSecurityGroup() {
			return securityGroup;
		}

		/**
		 * @param securityGroup
		 *            the securityGroup to set
		 */
		public void setSecurityGroup(String securityGroup) {
			this.securityGroup = securityGroup;
		}

		/**
		 * @return the networks
		 */
		public List<String> getNetworks() {
			return networks;
		}

		/**
		 * @param networks
		 *            the networks to set
		 */
		public void setNetworks(List<String> networks) {
			this.networks = networks;
		}

		/**
		 * @return the availableZone
		 */
		public String getAvailableZone() {
			return availableZone;
		}

		/**
		 * @param availableZone
		 *            the availableZone to set
		 */
		public void setAvailableZone(String availableZone) {
			this.availableZone = availableZone;
		}

		/**
		 * @return the floatingNetwork
		 */
		public String getFloatingNetwork() {
			return floatingNetwork;
		}

		/**
		 * @param floatingNetwork the floatingNetwork to set
		 */
		public void setFloatingNetwork(String floatingNetwork) {
			this.floatingNetwork = floatingNetwork;
		}

	}
}
