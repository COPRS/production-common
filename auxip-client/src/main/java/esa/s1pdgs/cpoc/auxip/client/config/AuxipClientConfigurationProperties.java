package esa.s1pdgs.cpoc.auxip.client.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource({"${auxipConfigFile:classpath:auxip.properties}"})
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "auxip")
public class AuxipClientConfigurationProperties {
	
	public static class AuxipHostConfiguration {
		private String serviceRootUri;
		private String user;
		private String pass;
		private boolean trustSelfSignedCertificate = false;
		private int connectTimeoutSec = 10;
		private boolean enablePreemptiveAuthentication = true;
		private String creationDateAttributeName; // in legacy PRIP instances 'PublicationDate', in cloud PRIP 'creationDate'
		private String productNameAttrName; // in legacy PRIP instances 'Name', in cloud PRIP 'name'
		private String idAttrName; // in legacy PRIP instances 'Id', in cloud PRIP 'id'
		
		// - - - - - - - - - - - - - - - - - -
		
		@Override
		public String toString() {
			return "AuxipHostConfiguration [serviceRootUri=" + this.serviceRootUri + ", user=" + this.user
					+ ", pass=****, trustSelfSignedCertificate=" + this.trustSelfSignedCertificate
					+ ", connectTimeoutSec=" + this.connectTimeoutSec + ", creationDateAttributeName="
					+ this.creationDateAttributeName + ", productNameAttrName=" + this.productNameAttrName
					+ ", idAttrName=" + this.idAttrName + ", enablePreemptiveAuthentication="
					+ this.enablePreemptiveAuthentication + "]";
		}

		// - - - - - - - - - - - - - - - - - -
		
		public String getServiceRootUri() {
			return this.serviceRootUri;
		}

		public void setServiceRootUri(String serviceRootUri) {
			this.serviceRootUri = serviceRootUri;
		}
		
		public boolean isTrustSelfSignedCertificate() {
			return this.trustSelfSignedCertificate;
		}

		public void setTrustSelfSignedCertificate(final boolean trustSelfSignedCertificate) {
			this.trustSelfSignedCertificate = trustSelfSignedCertificate;
		}

		public String getUser() {
			return this.user;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public String getPass() {
			return this.pass;
		}

		public void setPass(final String pass) {
			this.pass = pass;
		}
		
		public int getConnectTimeoutSec() {
			return this.connectTimeoutSec;
		}

		public void setConnectTimeoutSec(final int connectTimeoutSec) {
			this.connectTimeoutSec = connectTimeoutSec;
		}
		
		public boolean isEnablePreemptiveAuthentication() {
			return this.enablePreemptiveAuthentication;
		}

		public void setEnablePreemptiveAuthentication(boolean enablePreemptiveAuthentication) {
			this.enablePreemptiveAuthentication = enablePreemptiveAuthentication;
		}

		public String getCreationDateAttributeName() {
			return this.creationDateAttributeName;
		}

		public void setCreationDateAttributeName(String creationDateAttributeName) {
			this.creationDateAttributeName = creationDateAttributeName;
		}

		public String getProductNameAttrName() {
			return this.productNameAttrName;
		}

		public void setProductNameAttrName(String productNameAttrName) {
			this.productNameAttrName = productNameAttrName;
		}

		public String getIdAttrName() {
			return this.idAttrName;
		}

		public void setIdAttrName(String idAttrName) {
			this.idAttrName = idAttrName;
		}
	}
	
	// --------------------------------------------------------------------------
	
	private String proxyHost;
	private int proxyPort = 80;

	private List<AuxipHostConfiguration> hostConfigs;
	
	// --------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "AuxipClientConfigurationProperties [proxyHost=" + this.proxyHost + ", proxyPort=" + this.proxyPort
				+ ", hostConfigs=" + this.hostConfigs + "]";
	}
	
	// --------------------------------------------------------------------------

	public List<AuxipHostConfiguration> getHostConfigs() {
		return this.hostConfigs;
	}

	public void setHostConfigs(final List<AuxipHostConfiguration> hostConfigs) {
		this.hostConfigs = hostConfigs;
	}
	
	public String getProxyHost() {
		return this.proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return this.proxyPort;
	}

	public void setProxyPort(final int proxyPort) {
		this.proxyPort = proxyPort;
	}

}
