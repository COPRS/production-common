package esa.s1pdgs.cpoc.evictionmanagement.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("eviction-management-worker")
public class WorkerConfigurationProperties {
	
	private long pollingIntervalMs = 1000;
	private long pollingInitialDelayMs = 5000;
	private String hostname;
	
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}
	public void setPollingIntervalMs(long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	public long getPollingInitialDelayMs() {
		return pollingInitialDelayMs;
	}
	public void setPollingInitialDelayMs(long pollingInitialDelayMs) {
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

}
