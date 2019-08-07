package esa.s1pdgs.cpoc.disseminator.config;

import java.util.HashMap;
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
			FTPS
		}
		
		private Protocol protocol = Protocol.FILE;
		private String path = null;
		private String username = null;
		private String password = null;
		private String keyFile = null;
		
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
		
		public String getKeyFile() {
			return keyFile;
		}
		
		public void setKeyFile(String keyFile) {
			this.keyFile = keyFile;
		}

		@Override
		public String toString() {
			return "OutboxConfiguration [protocol=" + protocol + ", path=" + path + ", username=" + username
					+ ", password=<NOT_SHOWN>, keyFile=" + keyFile + "]";
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
	
	
	private long pollingIntervalMs = 100;
	private int maxRetries = 2;	
	private long tempoRetryMs = 100;
	private String hostname = "localhost";
    private Map<String, OutboxConfiguration> outboxes = new HashMap<>();
	private Map<ProductCategory, List<DisseminationTypeConfiguration>> categories = new HashMap<>();
	
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

	@Override
	public String toString() {
		return "DisseminationProperties [pollingIntervalMs=" + pollingIntervalMs + ", maxRetries=" + maxRetries
				+ ", tempoRetryMs=" + tempoRetryMs + ", hostname=" + hostname + ", outboxes=" + outboxes
				+ ", categories=" + categories + "]";
	}
}