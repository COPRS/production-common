package esa.s1pdgs.cpoc.prip.worker.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("prip-worker")
public class ApplicationProperties {
	private String hostname;
	private int metadataUnavailableRetriesNumber = 10;
	private long metadataUnavailableRetriesIntervalMs = 5000;
	
	public int getMetadataUnavailableRetriesNumber() {
		return metadataUnavailableRetriesNumber;
	}
	
	public void setMetadataUnavailableRetriesNumber(final int metadataUnavailableRetriesNumber) {
		this.metadataUnavailableRetriesNumber = metadataUnavailableRetriesNumber;
	}
	
	public long getMetadataUnavailableRetriesIntervalMs() {
		return metadataUnavailableRetriesIntervalMs;
	}
	
	public void setMetadataUnavailableRetriesIntervalMs(final long metadataUnavailableRetriesIntervalMs) {
		this.metadataUnavailableRetriesIntervalMs = metadataUnavailableRetriesIntervalMs;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}	
}
