package esa.s1pdgs.cpoc.ingestion.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ingestion-worker")
public class IngestionWorkerServiceConfigurationProperties {	
	private long pollingIntervalMs = 100;
	private int maxRetries = 2;	
	private long tempoRetryMs = 100;
	private String hostname = "localhost";
	private boolean bufferInputs = false;
	
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}
	
	public void setPollingIntervalMs(final long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(final String hostname) {
		this.hostname = hostname;
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

	public boolean isBufferInputs() {
		return bufferInputs;
	}

	public void setBufferInputs(final boolean bufferInputs) {
		this.bufferInputs = bufferInputs;
	}	
}
