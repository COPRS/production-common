package esa.s1pdgs.cpoc.disseminator.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dissemination")
public class DisseminationProperties {	
	public static class OutboxConfiguration {
		public enum Protocol {
			FILE,
			SFTP,
			FTPS,
			FTP
		}
		
		private Protocol protocol = Protocol.FILE;
		private String path = null;
		private String username = null;
		private String password = null;
		private String keyData = null;
		private String hostname = "localhost";
		private int port = -1; // --> not defined
		
		private String filePermissions = null;
		private String directoryPermissions = null;
		
		private String keystoreFile = null;
		private String keystorePass = "changeit";
		private String pathEvaluator = null;
		
		// per default, use java keystore and password
		private String truststoreFile = System.getProperty("java.home") + "/lib/security/cacerts";
		private String truststorePass = "changeit";
		
		private int bufferSize = 8 * 1014 * 1024;
		
		private boolean implicitSsl = false;
		
		private boolean ftpPasv = false;
		private boolean ftpsSslSessionReuse = true;
		private boolean useExtendedMasterSecret = false;
		
		private boolean skipExisting = true;
		
		private String chmodScriptPath = null;
						
		public Protocol getProtocol() {
			return protocol;
		}
		
		public void setProtocol(final Protocol protocol) {
			this.protocol = protocol;
		}
		
		public String getPath() {
			return path;
		}
		
		public void setPath(final String path) {
			this.path = path;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(final String username) {
			this.username = username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword(final String password) {
			this.password = password;
		}

		public String getKeyData() {
			return keyData;
		}

		public void setKeyData(final String keyData) {
			this.keyData = keyData;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(final String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return port;
		}

		public void setPort(final int port) {
			this.port = port;
		}

		public String getKeystoreFile() {
			return keystoreFile;
		}

		public void setKeystoreFile(final String keystoreFile) {
			this.keystoreFile = keystoreFile;
		}

		public String getKeystorePass() {
			return keystorePass;
		}

		public void setKeystorePass(final String keystorePass) {
			this.keystorePass = keystorePass;
		}

		public String getTruststoreFile() {
			return truststoreFile;
		}

		public void setTruststoreFile(final String truststoreFile) {
			this.truststoreFile = truststoreFile;
		}

		public String getTruststorePass() {
			return truststorePass;
		}

		public void setTruststorePass(final String truststorePass) {
			this.truststorePass = truststorePass;
		}
		
		public boolean isImplicitSsl() {
			return implicitSsl;
		}

		public void setImplicitSsl(final boolean implicitSsl) {
			this.implicitSsl = implicitSsl;
		}
		
		public String getPathEvaluator() {
			return pathEvaluator;
		}

		public void setPathEvaluator(final String pathEvaluator) {
			this.pathEvaluator = pathEvaluator;
		}
		
		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(final int bufferSize) {
			this.bufferSize = bufferSize;
		}
		
		public boolean isFtpPasv() {
			return ftpPasv;
		}

		public void setFtpPasv(final boolean ftpPasv) {
			this.ftpPasv = ftpPasv;
		}
		
		public boolean getFtpsSslSessionReuse() {
			return ftpsSslSessionReuse;
		}

		public void setFtpsSslSessionReuse(boolean ftpsSslSessionReuse) {
			this.ftpsSslSessionReuse = ftpsSslSessionReuse;
		}

		public boolean getUseExtendedMasterSecret() {
			return useExtendedMasterSecret;
		}

		public void setExtendedMasterSecret(boolean useExtendedMasterSecret) {
			this.useExtendedMasterSecret = useExtendedMasterSecret;
		}
		
		public boolean isSkipExisting() {
			return skipExisting;
		}

		public void setSkipExisting(final boolean skipExisting) {
			this.skipExisting = skipExisting;
		}

		public String getChmodScriptPath() {
			return chmodScriptPath;
		}

		public void setChmodScriptPath(final String chmodScriptPath) {
			this.chmodScriptPath = chmodScriptPath;
		}
		
		public String getFilePermissions() {
			return filePermissions;
		}

		public void setFilePermissions(final String filePermissions) {
			this.filePermissions = filePermissions;
		}

		public String getDirectoryPermissions() {
			return directoryPermissions;
		}

		public void setDirectoryPermissions(final String directoryPermissions) {
			this.directoryPermissions = directoryPermissions;
		}

		@Override
		public String toString() {
			return "OutboxConfiguration [protocol=" + protocol + ", path=" + path + ", username=" + username
					+ ", password=<NOT_SHOWN>, keyData=<NOT_SHOWN>, hostname=" + hostname + ", port=" + port +
					", directoryPermissions=" + directoryPermissions +	", filePermissions=" + filePermissions + ", pathEvaluator=" + pathEvaluator + ", bufferSize=" 
					+ bufferSize+ ", ftpPasv=" + ftpPasv
					+ ", ftpsSslSessionReuse=" + ftpsSslSessionReuse + ", useExtendedMasterSecret=" + useExtendedMasterSecret
					+ ", keystoreFile=" + keystoreFile + ", keystorePass=<NOT_SHOWN>, truststoreFile="
					+ truststoreFile + ", truststorePass=<NOT_SHOWN>, implicitSsl=" + implicitSsl 
					+ ", skipExisting=" + skipExisting + ", chmodScriptPath=" + chmodScriptPath + "]";
		}
	}
	
	public static class DisseminationTypeConfiguration {
		private String target;
		private String regex;

		public String getTarget() {
			return target;
		}

		public void setTarget(final String target) {
			this.target = target;
		}

		public String getRegex() {
			return regex;
		}

		public void setRegex(final String regex) {
			this.regex = regex;
		}

		@Override
		public String toString() {
			return "DisseminationTypeConfiguration [target=" + target + ", regex=" + regex + "]";
		}
	}
	
	
	private long pollingIntervalMs = 1000;
	private int maxRetries = 2;	
	private long tempoRetryMs = 100;
	private String hostname = "localhost";
    private Map<String, OutboxConfiguration> outboxes = new LinkedHashMap<>();
	private Map<ProductCategory, List<DisseminationTypeConfiguration>> categories = new LinkedHashMap<>();
	
	private boolean disableOverpassCheck = true;
	private String overpassCoverageCheckPattern = "$a"; // per default, don't match anything
	private int minOverpassCoveragePercentage = 100;
	
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}
	
	public void setPollingIntervalMs(final long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	
	public int getMaxRetries() {
		return maxRetries;
	}
	
	public void setMaxRetries(final int maxRetries) {
		this.maxRetries = maxRetries;
	}
	
	public long getTempoRetryMs() {
		return tempoRetryMs;
	}
	
	public void setTempoRetryMs(final long tempoRetryMs) {
		this.tempoRetryMs = tempoRetryMs;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}
	
	public Map<String, OutboxConfiguration> getOutboxes() {
		return outboxes;
	}

	public void setOutboxes(final Map<String, OutboxConfiguration> outboxes) {
		this.outboxes = outboxes;
	}

	public Map<ProductCategory, List<DisseminationTypeConfiguration>> getCategories() {
		return categories;
	}
	
	public void setCategories(final Map<ProductCategory, List<DisseminationTypeConfiguration>> categories) {
		this.categories = categories;
	}

	public boolean isDisableOverpassCheck() {
		return disableOverpassCheck;
	}

	public void setDisableOverpassCheck(final boolean disableOverpassCheck) {
		this.disableOverpassCheck = disableOverpassCheck;
	}

	public String getOverpassCoverageCheckPattern() {
		return overpassCoverageCheckPattern;
	}

	public void setOverpassCoverageCheckPattern(final String overpassCoverageCheckPattern) {
		this.overpassCoverageCheckPattern = overpassCoverageCheckPattern;
	}

	public int getMinOverpassCoveragePercentage() {
		return minOverpassCoveragePercentage;
	}

	public void setMinOverpassCoveragePercentage(final int minOverpassCoveragePercentage) {
		this.minOverpassCoveragePercentage = minOverpassCoveragePercentage;
	}
	
	@Override
	public String toString() {
		return "DisseminationProperties [pollingIntervalMs=" + pollingIntervalMs + ", maxRetries=" + maxRetries
				+ ", tempoRetryMs=" + tempoRetryMs + ", hostname=" + hostname + ", outboxes=" + outboxes
				+ ", categories=" + categories + ", disableOverpassCheck=" + disableOverpassCheck
				+ ", minOverpassCoveragePercentage=" + minOverpassCoveragePercentage
				+ ", overpassCoverageCheckPattern=" + overpassCoverageCheckPattern + "]";
	}
}