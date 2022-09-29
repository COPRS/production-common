package esa.s1pdgs.cpoc.ebip.client.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "edip")
public class EdipClientConfigurationProperties {
	public static class EdipHostConfiguration {
		private String serverName;
		private String user;
		private String pass;
		private boolean trustSelfSignedCertificate = false;
		private boolean encryptDataChannel = false;
		private boolean ftpsSslSessionReuse = true;
		private boolean useExtendedMasterSecret = false;
		private int connectTimeoutSec = 60;
		private boolean pasv;
		private boolean enableHostnameVerification = false;
		private String sslProtocol = "TLSv1.2";
		private boolean explicitFtps = true;
		private String keyManagerKeyStore =  "";
		private String keyManagerKeyStorePassword = "changeit"; // default JKS password
		private String trustManagerKeyStore =  "";
		private String trustManagerKeyStorePassword = "changeit"; // default JKS password
						
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

		public String getSslProtocol() {
			return sslProtocol;
		}

		public void setSslProtocol(final String sslProtocol) {
			this.sslProtocol = sslProtocol;
		}

		public boolean isExplicitFtps() {
			return explicitFtps;
		}

		public void setExplicitFtps(final boolean explicitFtps) {
			this.explicitFtps = explicitFtps;
		}

		public String getKeyManagerKeyStore() {
			return keyManagerKeyStore;
		}

		public void setKeyManagerKeyStore(final String keyManagerKeyStore) {
			this.keyManagerKeyStore = keyManagerKeyStore;
		}

		public String getKeyManagerKeyStorePassword() {
			return keyManagerKeyStorePassword;
		}

		public void setKeyManagerKeyStorePassword(final String keyManagerKeyStorePassword) {
			this.keyManagerKeyStorePassword = keyManagerKeyStorePassword;
		}

		public String getTrustManagerKeyStore() {
			return trustManagerKeyStore;
		}

		public void setTrustManagerKeyStore(final String trustManagerKeyStore) {
			this.trustManagerKeyStore = trustManagerKeyStore;
		}

		public String getTrustManagerKeyStorePassword() {
			return trustManagerKeyStorePassword;
		}

		public void setTrustManagerKeyStorePassword(final String trustManagerKeyStorePassword) {
			this.trustManagerKeyStorePassword = trustManagerKeyStorePassword;
		}

		public boolean isEncryptDataChannel() {
			return encryptDataChannel;
		}

		public void setEncryptDataChannel(boolean encryptDataChannel) {
			this.encryptDataChannel = encryptDataChannel;
		}

		public boolean isFtpsSslSessionReuse() {
			return ftpsSslSessionReuse;
		}

		public void setFtpsSslSessionReuse(boolean ftpsSslSessionReuse) {
			this.ftpsSslSessionReuse = ftpsSslSessionReuse;
		}

		public boolean isUseExtendedMasterSecret() {
			return useExtendedMasterSecret;
		}

		public void setUseExtendedMasterSecret(boolean useExtendedMasterSecret) {
			this.useExtendedMasterSecret = useExtendedMasterSecret;
		}

		public boolean isEnableHostnameVerification() {
			return enableHostnameVerification;
		}

		public void setEnableHostnameVerification(boolean enableHostnameVerification) {
			this.enableHostnameVerification = enableHostnameVerification;
		}
	}
	
	private String proxyHost;
	private int proxyPort = 80;

	private Map<String, EdipHostConfiguration> hostConfigs = new HashMap<>();

	public Map<String, EdipHostConfiguration> getHostConfigs() {
		return hostConfigs;
	}

	public void setHostConfigs(final Map<String, EdipHostConfiguration> hostConfigs) {
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
