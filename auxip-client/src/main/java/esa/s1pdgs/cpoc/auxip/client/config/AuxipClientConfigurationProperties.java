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
		private String creationDateAttributeName; // in legacy PRIP instances 'PublicationDate', in cloud PRIP 'creationDate'
		private String productNameAttrName; // in legacy PRIP instances 'Name', in cloud PRIP 'name'
		private String idAttrName; // in legacy PRIP instances 'Id', in cloud PRIP 'id'
		
		// - - - - - - - - - - - - - - - - - -
		
		@Override
		public String toString() {
			return "AuxipHostConfiguration [serviceRootUri=" + this.serviceRootUri + ", user=" + this.user
					+ ", pass=****" + ", creationDateAttributeName=" + this.creationDateAttributeName
					+ ", productNameAttrName=" + this.productNameAttrName + ", idAttrName=" + this.idAttrName + "]";
		}

		// - - - - - - - - - - - - - - - - - -
		
		public String getServiceRootUri() {
			return this.serviceRootUri;
		}

		public void setServiceRootUri(String serviceRootUri) {
			this.serviceRootUri = serviceRootUri;
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
	
	private String proxy;

	private List<AuxipHostConfiguration> hostConfigs;
	
	// --------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return "AuxipClientConfigurationProperties [proxy=" + this.proxy + ", hostConfigs=" + this.hostConfigs + "]";
	}
	
	// --------------------------------------------------------------------------

	public List<AuxipHostConfiguration> getHostConfigs() {
		return this.hostConfigs;
	}

	public void setHostConfigs(final List<AuxipHostConfiguration> hostConfigs) {
		this.hostConfigs = hostConfigs;
	}
	
	public String getProxy() {
		return this.proxy;
	}

	public void setProxy(final String proxy) {
		this.proxy = proxy;
	}

}
