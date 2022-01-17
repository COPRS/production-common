package de.werum.coprs.nativeapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("native-api")
public class NativeApiProperties {

	private String majorVersion;
	private String version;

	private String pripProtocol;
	private String pripHost;
	private Integer pripPort;

	private String externalPripProtocol;
	private String externalPripHost;
	private Integer externalPripPort;

	private boolean includeAdditionalAttributes = true;

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

}
