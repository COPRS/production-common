package esa.s1pdgs.cpoc.auxip.client.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "auxip")
public class AuxipClientConfigurationProperties {
	
	public enum BearerTokenType {
		AUTHORIZATION,
		OAUTH2_ACCESS_TOKEN
	}

	public static class AuxipHostConfiguration {
		private String serviceRootUri;
		private String user;
		private String pass;
		private boolean sslValidation = true;
		private String authType; // basic|oauth2|disable
		private BearerTokenType bearerTokenType = BearerTokenType.AUTHORIZATION;
		private String oauthAuthUrl;
		private String oauthClientId;
		private String oauthClientSecret;

		private String creationDateAttributeName = "PublicationDate"; // in legacy PRIP instances 'PublicationDate', in cloud PRIP
													// 'creationDate'
		private String productNameAttrName = "Name"; // in legacy PRIP instances 'Name', in cloud PRIP 'name'
		private String idAttrName = "Id"; // in legacy PRIP instances 'Id', in cloud PRIP 'id'
		private String contentLengthAttrName="ContentLength"; // in legacy PRIP instances 'ContentLength', in cloud PRIP 'contentLength'
		private boolean useHttpClientDownload = true;

		// - - - - - - - - - - - - - - - - - -

		@Override
		public String toString() {
			return "AuxipHostConfiguration [serviceRootUri=" + this.serviceRootUri + ", user=" + this.user
					+ ", pass=****" + ", sslValidation=" + this.sslValidation + ", authType=" + this.authType
					+ ", oauthAuthUrl=" + this.oauthAuthUrl + ", oauthClientId=" + this.oauthClientId
					+ ", bearerTokenType=" + this.bearerTokenType + ", oauthClientSecret=" + this.oauthClientSecret
					+ ", creationDateAttributeName=" + this.creationDateAttributeName + ", productNameAttrName="
					+ this.productNameAttrName + ", contentLengthAttrName=" + this.contentLengthAttrName
					+ ", idAttrName=" + this.idAttrName + ", useHttpClientDownload=" + useHttpClientDownload + "]";
		}

		// - - - - - - - - - - - - - - - - - -

		public String getServiceRootUri() {
			return this.serviceRootUri;
		}

		public void setServiceRootUri(final String serviceRootUri) {
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

		public void setCreationDateAttributeName(final String creationDateAttributeName) {
			this.creationDateAttributeName = creationDateAttributeName;
		}

		public String getProductNameAttrName() {
			return this.productNameAttrName;
		}

		public void setProductNameAttrName(final String productNameAttrName) {
			this.productNameAttrName = productNameAttrName;
		}

		public String getIdAttrName() {
			return this.idAttrName;
		}

		public void setIdAttrName(final String idAttrName) {
			this.idAttrName = idAttrName;
		}

		public boolean isSslValidation() {
			return this.sslValidation;
		}

		public void setSslValidation(final boolean sslValidation) {
			this.sslValidation = sslValidation;
		}

		public String getContentLengthAttrName() {
			return this.contentLengthAttrName;
		}

		public void setContentLengthAttrName(String contentLengthAttrName) {
			this.contentLengthAttrName = contentLengthAttrName;
		}

		public boolean isUseHttpClientDownload() {
			return this.useHttpClientDownload;
		}

		public void setUseHttpClientDownload(final boolean useHttpClientDownload) {
			this.useHttpClientDownload = useHttpClientDownload;
		}

		public String getOauthAuthUrl() {
			return this.oauthAuthUrl;
		}

		public void setOauthAuthUrl(String oauthAuthUrl) {
			this.oauthAuthUrl = oauthAuthUrl;
		}

		public String getOauthClientId() {
			return this.oauthClientId;
		}

		public void setOauthClientId(String oauthClientId) {
			this.oauthClientId = oauthClientId;
		}

		public String getOauthClientSecret() {
			return this.oauthClientSecret;
		}

		public void setOauthClientSecret(String oauthClientSecret) {
			this.oauthClientSecret = oauthClientSecret;
		}

		public String getAuthType() {
			return this.authType;
		}

		public void setAuthType(String authType) {
			this.authType = authType;
		}

		public BearerTokenType getBearerTokenType() {
			return bearerTokenType;
		}

		public void setBearerTokenType(BearerTokenType bearerTokenType) {
			this.bearerTokenType = bearerTokenType;
		}

	}

	// --------------------------------------------------------------------------

	private String proxy;

	private Map<String, AuxipHostConfiguration> hostConfigs = new HashMap<>();

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return "AuxipClientConfigurationProperties [proxy=" + this.proxy + ", hostConfigs=" + this.hostConfigs + "]";
	}

	// --------------------------------------------------------------------------

	public Map<String, AuxipHostConfiguration> getHostConfigs() {
		return this.hostConfigs;
	}

	public void setHostConfigs(final Map<String, AuxipHostConfiguration> hostConfigs) {
		this.hostConfigs = hostConfigs;
	}

	public String getProxy() {
		return this.proxy;
	}

	public void setProxy(final String proxy) {
		this.proxy = proxy;
	}

}
