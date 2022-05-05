package esa.s1pdgs.cpoc.prip.worker.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("distribution-worker")
public class PripWorkerConfigurationProperties {
	
	public static class MetadataMapping {
		private String regexp;
		private Map<String, String> map = new LinkedHashMap<>();
		
		public String getRegexp() {
			return regexp;
		}
		public void setRegexp(String regexp) {
			this.regexp = regexp;
		}
		public Map<String, String> getMap() {
			return map;
		}
		public void setMap(Map<String, String> map) {
			this.map = map;
		}
	}
	
	private String hostname;
	private int metadataUnavailableRetriesNumber = 10;
	private long metadataUnavailableRetriesIntervalMs = 5000;
	private int metadataInsertionRetriesNumber = 3;
	private long metadataInsertionRetriesIntervalMs = 1000;
	private String footprintIsLineStringRegexp = "^$a";
	
	private Map<String, MetadataMapping> metadata = new LinkedHashMap<>();
	
	public int getMetadataUnavailableRetriesNumber() {
		return metadataUnavailableRetriesNumber;
	}
	
	public void setMetadataUnavailableRetriesNumber(final int metadataUnavailableRetriesNumber) {
		this.metadataUnavailableRetriesNumber = metadataUnavailableRetriesNumber;
	}
	
	public int getMetadataInsertionRetriesNumber() {
		return metadataInsertionRetriesNumber;
	}

	public void setMetadataInsertionRetriesNumber(int metadataInsertionRetriesNumber) {
		this.metadataInsertionRetriesNumber = metadataInsertionRetriesNumber;
	}

	public long getMetadataInsertionRetriesIntervalMs() {
		return metadataInsertionRetriesIntervalMs;
	}

	public void setMetadataInsertionRetriesIntervalMs(long metadataInsertionRetriesIntervalMs) {
		this.metadataInsertionRetriesIntervalMs = metadataInsertionRetriesIntervalMs;
	}
	
	public long getMetadataUnavailableRetriesIntervalMs() {
		return metadataUnavailableRetriesIntervalMs;
	}
	
	public void setMetadataUnavailableRetriesIntervalMs(final long metadataUnavailableRetriesIntervalMs) {
		this.metadataUnavailableRetriesIntervalMs = metadataUnavailableRetriesIntervalMs;
	}
	
	public String getFootprintIsLineStringRegexp() {
		return footprintIsLineStringRegexp;
	}

	public void setFootprintIsLineStringRegexp(String footprintIsLineStringRegexp) {
		this.footprintIsLineStringRegexp = footprintIsLineStringRegexp;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public Map<String, MetadataMapping> getMetadataMapping() {
		return metadata;
	}

	public void setMetadataMapping(Map<String, MetadataMapping> metadata) {
		this.metadata = metadata;
	}

}
