package de.werum.coprs.nativeapi.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("native-api")
public class NativeApiProperties {

	private String majorVersion;
	private String version;
	private Long downloadUrlExpirationTimeInSeconds = 600L;

	private String pripProtocol;
	private String pripHost;
	private Integer pripPort;

	private String externalPripProtocol;
	private String externalPripHost;
	private Integer externalPripPort;

	private List<AttributesOfMission> attributesOfMission = new LinkedList<>();

	public static class AttributesOfMission {
		private String missionName;
		private List<String> baseAttributes = new LinkedList<>();
		private List<AttributesOfProductType> attributesOfProductType = new LinkedList<>();

		public String getMissionName() {
			return this.missionName;
		}

		public void setMissionName(String missionName) {
			this.missionName = missionName;
		}

		public List<AttributesOfProductType> getAttributesOfProductType() {
			return this.attributesOfProductType;
		}

		public void setAttributesOfProductType(List<AttributesOfProductType> attributesOfProductType) {
			this.attributesOfProductType = attributesOfProductType;
		}

		public List<String> getBaseAttributes() {
			return this.baseAttributes;
		}

		public void setBaseAttributes(List<String> baseAttributes) {
			this.baseAttributes = baseAttributes;
		}
	}

	public static class AttributesOfProductType {
		private String productType;
		private Map<String, String> attributes = new LinkedHashMap<>();

		public String getProductType() {
			return this.productType;
		}
		public void setProductType(String productType) {
			this.productType = productType;
		}

		public Map<String, String> getAttributes() {
			return this.attributes;
		}

		public void setAttributes(Map<String, String> attributes) {
			this.attributes = attributes;
		}
	}

	public String getMajorVersion() {
		return this.majorVersion;
	}

	public void setMajorVersion(final String majorVersion) {
		this.majorVersion = majorVersion;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public List<AttributesOfMission> getAttributesOfMission() {
		return this.attributesOfMission;
	}

	public void setAttributesOfMission(final List<AttributesOfMission> attributesOfMission) {
		this.attributesOfMission = attributesOfMission;
	}

	public Long getDownloadUrlExpirationTimeInSeconds() {
		return this.downloadUrlExpirationTimeInSeconds;
	}

	public void setDownloadUrlExpirationTimeInSeconds(final Long downloadUrlExpirationTimeInSeconds) {
		this.downloadUrlExpirationTimeInSeconds = downloadUrlExpirationTimeInSeconds;
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

}
