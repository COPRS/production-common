package esa.s1pdgs.cpoc.ebip.client.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource({"${ebipConfigFile:classpath:ebip.properties}"})
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "edip")
public class EdipClientConfigurationProperties {
	public static class EdipHostConfiguration {
		private String serverName;
		private String user;
		private String pass;
		private boolean trustSelfSignedCertificate = false;
		private int connectTimeoutSec = 10;
		private boolean pasv;
		private String sslProtocol = "TLS";
		private boolean explictFtps = true;
		
		
		public String getServerName() {
			return serverName;
		}

		public void setServerName(final String serverName) {
			this.serverName = serverName;
		}

		public boolean isTrustSelfSignedCertificate() {
			return trustSelfSignedCertificate;
		}

		public void setTrustSelfSignedCertificate(final boolean trustSelfSignedCertificate) {
			this.trustSelfSignedCertificate = trustSelfSignedCertificate;
		}

		public String getUser() {
			return user;
		}

		public void setUser(final String user) {
			this.user = user;
		}

		public String getPass() {
			return pass;
		}

		public void setPass(final String pass) {
			this.pass = pass;
		}

		public int getConnectTimeoutSec() {
			return connectTimeoutSec;
		}

		public void setConnectTimeoutSec(final int connectTimeoutSec) {
			this.connectTimeoutSec = connectTimeoutSec;
		}
		public boolean isPasv() {
			return pasv;
		}

		public void setPasv(final boolean pasv) {
			this.pasv = pasv;
		}		
	}
	
	private String proxyHost;
	private int proxyPort = 80;

	private List<EdipHostConfiguration> hostConfigs;

	public List<EdipHostConfiguration> getHostConfigs() {
		return hostConfigs;
	}

	public void setHostConfigs(final List<EdipHostConfiguration> hostConfigs) {
		this.hostConfigs = hostConfigs;
	}
	
	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(final int proxyPort) {
		this.proxyPort = proxyPort;
	}

	@Override
	public String toString() {
		return "EdipClientConfigurationProperties [proxyHost=" + proxyHost + ", proxyPort=" + proxyPort
				+ ", hostConfigs=" + hostConfigs + "]";
	}
}
