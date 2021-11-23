package de.werum.coprs.ddip.frontend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ddip")
public class DdipProperties {

	private String majorVersion;
	private String version;

	private String dispatchPripProtocol;
	private String dispatchPripHost;
	private Integer dispatchPripPort;

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

	public String getDispatchPripProtocol() {
		return this.dispatchPripProtocol;
	}

	public void setDispatchPripProtocol(String dispatchPripProtocol) {
		this.dispatchPripProtocol = dispatchPripProtocol;
	}

	public String getDispatchPripHost() {
		return this.dispatchPripHost;
	}

	public void setDispatchPripHost(String dispatchPripHost) {
		this.dispatchPripHost = dispatchPripHost;
	}

	public Integer getDispatchPripPort() {
		return this.dispatchPripPort;
	}

	public void setDispatchPripPort(Integer dispatchPripPort) {
		this.dispatchPripPort = dispatchPripPort;
	}

}
