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
		
		private String keystoreFile = null;
		private String keystorePass = "changeit";
		private String pathEvaluator = null;
		
		// per default, use java keystore and password
		private String truststoreFile = System.getProperty("java.home") + "/lib/security/cacerts";
		private String truststorePass = "changeit";
		
		private int bufferSize = 8 * 1014 * 1024;
		
		private boolean implicitSsl = true;
		
		private boolean ftpPasv = false;
		
		private boolean skipExisting = true;
		
		private String chmodScriptPath = null;
						
		public Protocol getProtocol() {
			return protocol;
		}
		
		public void setProtocol(Protocol protocol) {
			this.protocol = protocol;
		}
		
		public String getPath() {
			return path;
		}
		
		public void setPath(String path) {
			this.path = path;
		}
		
		public String getUsername() {
			return username;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}
		
		public String getPassword() {
			return password;
		}
		
		public void setPassword(String password) {
			this.password = password;
		}

		public String getKeyData() {
			return keyData;
		}

		public void setKeyData(String keyData) {
			this.keyData = keyData;
		}

		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getKeystoreFile() {
			return keystoreFile;
		}

		public void setKeystoreFile(String keystoreFile) {
			this.keystoreFile = keystoreFile;
		}

		public String getKeystorePass() {
			return keystorePass;
		}

		public void setKeystorePass(String keystorePass) {
			this.keystorePass = keystorePass;
		}

		public String getTruststoreFile() {
			return truststoreFile;
		}

		public void setTruststoreFile(String truststoreFile) {
			this.truststoreFile = truststoreFile;
		}

		public String getTruststorePass() {
			return truststorePass;
		}

		public void setTruststorePass(String truststorePass) {
			this.truststorePass = truststorePass;
		}
		
		public boolean isImplicitSsl() {
			return implicitSsl;
		}

		public void setImplicitSsl(boolean implicitSsl) {
			this.implicitSsl = implicitSsl;
		}
		
		public String getPathEvaluator() {
			return pathEvaluator;
		}

		public void setPathEvaluator(String pathEvaluator) {
			this.pathEvaluator = pathEvaluator;
		}
		
		public int getBufferSize() {
			return bufferSize;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
		}
		
		public boolean isFtpPasv() {
			return ftpPasv;
		}

		public void setFtpPasv(boolean ftpPasv) {
			this.ftpPasv = ftpPasv;
		}
		
		public boolean isSkipExisting() {
			return skipExisting;
		}

		public void setSkipExisting(boolean skipExisting) {
			this.skipExisting = skipExisting;
		}

		public String getChmodScriptPath() {
			return chmodScriptPath;
		}

		public void setChmodScriptPath(String chmodScriptPath) {
			this.chmodScriptPath = chmodScriptPath;
		}

		@Override
		public String toString() {
			return "OutboxConfiguration [protocol=" + protocol + ", path=" + path + ", username=" + username
					+ ", password=<NOT_SHOWN>, keyData=<NOT_SHOWN>, hostname=" + hostname + ", port=" + port +
					", pathEvaluator=" + pathEvaluator + ", bufferSize=" + bufferSize+ ", ftpPasv=" + ftpPasv
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

		public void setTarget(String target) {
			this.target = target;
		}

		public String getRegex() {
			return regex;
		}

		public void setRegex(String regex) {
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
	
	public void setPollingIntervalMs(long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	
	public int getMaxRetries() {
		return maxRetries;
	}
	
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
	
	public long getTempoRetryMs() {
		return tempoRetryMs;
	}
	
	public void setTempoRetryMs(long tempoRetryMs) {
		this.tempoRetryMs = tempoRetryMs;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public Map<String, OutboxConfiguration> getOutboxes() {
		return outboxes;
	}

	public void setOutboxes(Map<String, OutboxConfiguration> outboxes) {
		this.outboxes = outboxes;
	}

	public Map<ProductCategory, List<DisseminationTypeConfiguration>> getCategories() {
		return categories;
	}
	
	public void setCategories(Map<ProductCategory, List<DisseminationTypeConfiguration>> categories) {
		this.categories = categories;
	}

	public boolean isDisableOverpassCheck() {
		return disableOverpassCheck;
	}

	public void setDisableOverpassCheck(boolean disableOverpassCheck) {
		this.disableOverpassCheck = disableOverpassCheck;
	}

	public String getOverpassCoverageCheckPattern() {
		return overpassCoverageCheckPattern;
	}

	public void setOverpassCoverageCheckPattern(String overpassCoverageCheckPattern) {
		this.overpassCoverageCheckPattern = overpassCoverageCheckPattern;
	}

	public int getMinOverpassCoveragePercentage() {
		return minOverpassCoveragePercentage;
	}

	public void setMinOverpassCoveragePercentage(int minOverpassCoveragePercentage) {
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