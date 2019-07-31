package esa.s1pdgs.cpoc.ingestion.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("ingestion")
public class IngestionServiceConfigurationProperties {	
	private long pollingIntervalMs;
	private int maxRetries;	
	private long tempoRetryMs;
	private String hostname;
	private List<IngestionTypeConfiguration> types;
	
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}
	
	public void setPollingIntervalMs(long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
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

	public List<IngestionTypeConfiguration> getTypes() {
		return types;
	}

	public void setTypes(List<IngestionTypeConfiguration> types) {
		this.types = types;
	}
}
