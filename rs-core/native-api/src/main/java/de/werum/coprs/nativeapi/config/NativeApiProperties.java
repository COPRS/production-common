package de.werum.coprs.nativeapi.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("native-api")
public class NativeApiProperties {
	private String pripProtocol;
	private String pripHost;
	private Integer pripPort;

	private String externalPripProtocol;
	private String externalPripHost;
	private Integer externalPripPort;
	
	private Integer defaultLimit = 100;
	private Integer maxLimit = 100;

	private boolean includeAdditionalAttributes = true;
	
	private String rootCatalogId;
	private String rootCatalogTitle;
	private String rootCatalogDescription;
	
	private String serviceDocLink;
	private String serviceDocMimeType;
	
	private String hostname;
	
	private Map<String, StacCollectionProperties> collections = new LinkedHashMap<>();
	
	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	private Map<String,List<String>> lutConfigs = new HashMap<>();

	public Map<String, List<String>> getLutConfigs() {
		return lutConfigs;
	}

	public void setLutConfigs(Map<String, List<String>> lutConfigs) {
		this.lutConfigs = lutConfigs;
	}

	public String getPripProtocol() {
		return this.pripProtocol;
	}

	public void setPripProtocol(String pripProtocol) {
		this.pripProtocol = pripProtocol;
	}

	public String getPripHost() {
		return this.pripHost;
	}

	public void setPripHost(String pripHost) {
		this.pripHost = pripHost;
	}

	public Integer getPripPort() {
		return this.pripPort;
	}

	public void setPripPort(Integer pripPort) {
		this.pripPort = pripPort;
	}

	public String getExternalPripProtocol() {
		return this.externalPripProtocol;
	}

	public void setExternalPripProtocol(String externalPripProtocol) {
		this.externalPripProtocol = externalPripProtocol;
	}

	public String getExternalPripHost() {
		return this.externalPripHost;
	}

	public void setExternalPripHost(String externalPripHost) {
		this.externalPripHost = externalPripHost;
	}

	public Integer getExternalPripPort() {
		return this.externalPripPort;
	}

	public void setExternalPripPort(Integer externalPripPort) {
		this.externalPripPort = externalPripPort;
	}

	public boolean getIncludeAdditionalAttributes() {
		return this.includeAdditionalAttributes;
	}

	public void setIncludeAdditionalAttributes(boolean includeAdditionalAttributes) {
		this.includeAdditionalAttributes = includeAdditionalAttributes;
	}

	public Integer getDefaultLimit() {
		return defaultLimit;
	}

	public void setDefaultLimit(Integer defaultLimit) {
		this.defaultLimit = defaultLimit;
	}

	public Integer getMaxLimit() {
		return maxLimit;
	}

	public void setMaxLimit(Integer maxLimit) {
		this.maxLimit = maxLimit;
	}

	public String getRootCatalogId() {
		return rootCatalogId;
	}

	public void setRootCatalogId(String rootCatalogId) {
		this.rootCatalogId = rootCatalogId;
	}

	public String getRootCatalogTitle() {
		return rootCatalogTitle;
	}

	public void setRootCatalogTitle(String rootCatalogTitle) {
		this.rootCatalogTitle = rootCatalogTitle;
	}

	public String getRootCatalogDescription() {
		return rootCatalogDescription;
	}

	public void setRootCatalogDescription(String rootCatalogDescription) {
		this.rootCatalogDescription = rootCatalogDescription;
	}

	public Map<String, StacCollectionProperties> getCollections() {
		return collections;
	}

	public void setCollections(Map<String, StacCollectionProperties> collections) {
		this.collections = collections;
	}
	
	public String getServiceDocLink() {
		return serviceDocLink;
	}

	public void setServiceDocLink(String serviceDocLink) {
		this.serviceDocLink = serviceDocLink;
	}

	public String getServiceDocMimeType() {
		return serviceDocMimeType;
	}

	public void setServiceDocMimeType(String serviceDocMimeType) {
		this.serviceDocMimeType = serviceDocMimeType;
	}

	public static class StacCollectionProperties {
		private String title;
		private String description;
		private String license;
		private String catalog;
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getLicense() {
			return license;
		}
		public void setLicense(String license) {
			this.license = license;
		}
		public String getCatalog() {
			return catalog;
		}
		public void setCatalog(String catalog) {
			this.catalog = catalog;
		}
	}
}
