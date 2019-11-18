package esa.s1pdgs.cpoc.ingestion.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ingestion")
public class IngestionServiceConfigurationProperties {	
	private long pollingIntervalMs = 100;
	private int maxRetries = 2;	
	private long tempoRetryMs = 100;
	private String hostname = "localhost";
	private List<IngestionTypeConfiguration> types = new ArrayList<>();
	
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
