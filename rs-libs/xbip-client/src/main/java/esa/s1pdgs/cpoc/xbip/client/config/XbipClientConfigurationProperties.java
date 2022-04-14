package esa.s1pdgs.cpoc.xbip.client.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource({"${xbipConfigFile:classpath:xbip.properties}"})
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "xbip")
public class XbipClientConfigurationProperties {
	public static class XbipHostConfiguration {
		private String serverName;
		private String user;
		private String pass;
		private boolean trustSelfSignedCertificate = false;
		private boolean programmaticRecursion = false;
		private int connectTimeoutSec = 60;
		private boolean enablePreemptiveAuthentication = true;
		private int numRetries = 5;
		private long retrySleepMs = 3000;
		
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
		
		public boolean getProgrammaticRecursion() {
			return programmaticRecursion;
		}

		public void setProgrammaticRecursion(final boolean programmaticRecursion) {
			this.programmaticRecursion = programmaticRecursion;
		}
		
		public boolean isEnablePreemptiveAuthentication() {
			return enablePreemptiveAuthentication;
		}

		public void setEnablePreemptiveAuthentication(boolean enablePreemptiveAuthentication) {
			this.enablePreemptiveAuthentication = enablePreemptiveAuthentication;
		}

		public int getNumRetries() {
			return numRetries;
		}

		public void setNumRetries(int numRetries) {
			this.numRetries = numRetries;
		}

		public long getRetrySleepMs() {
			return retrySleepMs;
		}

		public void setRetrySleepMs(long retrySleepMs) {
			this.retrySleepMs = retrySleepMs;
		}

		@Override
		public String toString() {
			return "XbipHostConfiguration [serverName=" + serverName + ", user=" + user + 
					", pass=****, trustSelfSignedCertificate=" + trustSelfSignedCertificate + 
					", connectTimeoutSec=" + connectTimeoutSec +", programmaticRecursion=" + programmaticRecursion +
					", enablePreemptiveAuthentication="	+ enablePreemptiveAuthentication + ", numRetries=" + numRetries +
					", retrySleepMs=" + retrySleepMs + "]";
		}
	}
	
	private String proxyHost;
	private int proxyPort = 80;

	private Map<String, XbipHostConfiguration> hostConfigs = new HashMap<>();

	public Map<String, XbipHostConfiguration> getHostConfigs() {
		return hostConfigs;
	}

	public void setHostConfigs(final Map<String, XbipHostConfiguration> hostConfigs) {
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
		return "XbipClientConfigurationProperties [proxyHost=" + proxyHost + ", proxyPort=" + proxyPort
				+ ", hostConfigs=" + hostConfigs + "]";
	}
}
