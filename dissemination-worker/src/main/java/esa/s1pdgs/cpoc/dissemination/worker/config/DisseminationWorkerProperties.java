package esa.s1pdgs.cpoc.dissemination.worker.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.utils.StringUtil;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dissemination-worker")
public class DisseminationWorkerProperties {

	private Map<String, OutboxConfiguration> outboxes = new LinkedHashMap<>(); // defining outboxes
	private List<OutboxConnection> outboxConnections = new ArrayList<>(); // connecting incoming jobs with outboxes

	private int maxRetries = 3;
	private long tempoRetryMs = 1000;

	private int obsMaxRetries = 100;
	private long obsTempoRetryMs = 10000;

	private long pollingIntervalMs = 1000;
	private long pollingInitialDelayMs = 5000;

	// --------------------------------------------------------------------------

	@Override
	public String toString() {
		return "DisseminationWorkerProperties [pollingIntervalMs=" + this.pollingIntervalMs + ", pollingInitialDelayMs="
				+ this.pollingInitialDelayMs + ", obsMaxRetries=" + this.obsMaxRetries + ", obsTempoRetryMs="
				+ this.obsTempoRetryMs + ", outboxes=" + this.outboxes + ", outboxConnections=" + this.outboxConnections + "]";
	}

	// --------------------------------------------------------------------------

	public long getPollingIntervalMs() {
		return this.pollingIntervalMs;
	}

	public void setPollingIntervalMs(long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}

	public long getPollingInitialDelayMs() {
		return this.pollingInitialDelayMs;
	}

	public void setPollingInitialDelayMs(long pollingInitialDelayMs) {
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}

	public Map<String, OutboxConfiguration> getOutboxes() {
		return this.outboxes;
	}

	public void setOutboxes(Map<String, OutboxConfiguration> outboxes) {
		this.outboxes = outboxes;
	}

	public List<OutboxConnection> getOutboxConnections() {
		return this.outboxConnections;
	}

	public void setOutboxConnections(List<OutboxConnection> outboxConnections) {
		this.outboxConnections = outboxConnections;
	}

	public int getMaxRetries() {
		return this.maxRetries;
	}

	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public long getTempoRetryMs() {
		return this.tempoRetryMs;
	}

	public void setTempoRetryMs(long tempoRetryMs) {
		this.tempoRetryMs = tempoRetryMs;
	}

	public int getObsMaxRetries() {
		return this.obsMaxRetries;
	}

	public void setObsMaxRetries(int obsMaxRetries) {
		this.obsMaxRetries = obsMaxRetries;
	}

	public long getObsTempoRetryMs() {
		return this.obsTempoRetryMs;
	}

	public void setObsTempoRetryMs(long obsTempoRetryMs) {
		this.obsTempoRetryMs = obsTempoRetryMs;
	}

	// --------------------------------------------------------------------------

	public static class OutboxConnection {
		private String matchRegex;
		private String outboxName;

		public String getMatchRegex() {
			return this.matchRegex;
		}

		public void setMatchRegex(String matchRegex) {
			this.matchRegex = matchRegex;
		}

		public String getOutboxName() {
			return this.outboxName;
		}

		public void setOutboxName(String outboxName) {
			this.outboxName = outboxName;
		}

		@Override
		public String toString() {
			return "OutboxConnection [outboxName=" + this.outboxName + ", matchRegex=" + this.getMatchRegex() + "]";
		}
	}

	// --------------------------------------------------------------------------

	public static class OutboxConfiguration {
		public enum Protocol {
			FTP, FTPS
		}

		private String hostname = "localhost";
		private Protocol protocol = Protocol.FTP;
		private int port = -1; // --> not defined
		private boolean implicitSsl = true;
		private boolean ftpPassiveMode = false;
		private String username = null;
		private String password = null;
		private String path = null;
		private String pathEvaluator = null;

		private String keyFile = null;
		private String keystoreFile = null;
		private String keystorePass = "changeit";

		// per default, use java keystore and password
		private String truststoreFile = System.getProperty("java.home") + "/lib/security/cacerts";
		private String truststorePass = "changeit";

		private int bufferSize = 8 * 1014 * 1024;

		private boolean skipExisting = true;

		private String chmodScriptPath = null;

		public Protocol getProtocol() {
			return this.protocol;
		}

		public void setProtocol(Protocol protocol) {
			this.protocol = protocol;
		}

		public String getPath() {
			return this.path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getPathEvaluator() {
			return this.pathEvaluator;
		}

		public void setPathEvaluator(String pathEvaluator) {
			this.pathEvaluator = pathEvaluator;
		}

		public String getUsername() {
			return this.username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return this.password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getKeyFile() {
			return this.keyFile;
		}

		public void setKeyFile(String keyFile) {
			this.keyFile = keyFile;
		}

		public String getHostname() {
			return this.hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getPort() {
			return this.port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getKeystoreFile() {
			return this.keystoreFile;
		}

		public void setKeystoreFile(String keystoreFile) {
			this.keystoreFile = keystoreFile;
		}

		public String getKeystorePass() {
			return this.keystorePass;
		}

		public void setKeystorePass(String keystorePass) {
			this.keystorePass = keystorePass;
		}

		public String getTruststoreFile() {
			return this.truststoreFile;
		}

		public void setTruststoreFile(String truststoreFile) {
			this.truststoreFile = truststoreFile;
		}

		public String getTruststorePass() {
			return this.truststorePass;
		}

		public void setTruststorePass(String truststorePass) {
			this.truststorePass = truststorePass;
		}

		public boolean isImplicitSsl() {
			return this.implicitSsl;
		}

		public void setImplicitSsl(boolean implicitSsl) {
			this.implicitSsl = implicitSsl;
		}

		public int getBufferSize() {
			return this.bufferSize;
		}

		public void setBufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
		}

		public void setFtpPasv(String ftpPasv) {
			this.ftpPassiveMode = parseBooleanOrDefault(ftpPasv, false);
		}

		public boolean isFtpPassiveMode() {
			return this.ftpPassiveMode;
		}

		public boolean isSkipExisting() {
			return this.skipExisting;
		}

		public void setSkipExisting(boolean skipExisting) {
			this.skipExisting = skipExisting;
		}

		public String getChmodScriptPath() {
			return this.chmodScriptPath;
		}

		public void setChmodScriptPath(String chmodScriptPath) {
			this.chmodScriptPath = chmodScriptPath;
		}

		@Override
		public String toString() {
			return "OutboxConfiguration [protocol=" + this.protocol + ", path=" + this.path + ", username="
					+ this.username + ", password=<NOT_SHOWN>, keyFile=" + this.keyFile + ", hostname=" + this.hostname
					+ ", port=" + this.port + ", bufferSize=" + this.bufferSize + ", ftpPassiveMode="
					+ this.ftpPassiveMode + ", keystoreFile=" + this.keystoreFile
					+ ", keystorePass=<NOT_SHOWN>, truststoreFile=" + this.truststoreFile
					+ ", truststorePass=<NOT_SHOWN>, implicitSsl=" + this.implicitSsl + ", skipExisting="
					+ this.skipExisting + ", chmodScriptPath=" + this.chmodScriptPath + "]";
		}

		private static boolean parseBooleanOrDefault(String booleanStr, boolean defaultValue) {
			if (StringUtil.isNotBlank(booleanStr)) {
				if ("true".equalsIgnoreCase(booleanStr) || "yes".equalsIgnoreCase(booleanStr)
						|| "on".equalsIgnoreCase(booleanStr) || "1".equals(booleanStr)) {
					return true;
				} else if ("false".equalsIgnoreCase(booleanStr) || "no".equalsIgnoreCase(booleanStr)
						|| "off".equalsIgnoreCase(booleanStr) || "0".equals(booleanStr)) {
					return false;
				}
			}

			return defaultValue;
		}
	}

}
