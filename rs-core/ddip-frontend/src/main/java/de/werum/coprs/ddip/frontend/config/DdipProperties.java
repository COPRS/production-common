package de.werum.coprs.ddip.frontend.config;

import java.net.URL;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ddip")
public class DdipProperties {

	private String majorVersion;
	private String version;
	private URL dispatchPripUrl;

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

	public URL getDispatchPripUrl() {
		return this.dispatchPripUrl;
	}

	public void setDispatchPripUrl(URL dispatchPripUrl) {
		this.dispatchPripUrl = dispatchPripUrl;
	}

}
