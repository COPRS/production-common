package esa.s1pdgs.cpoc.disseminator.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.ingestion.config.IngestionTypeConfiguration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "dissemination")
public class DisseminationProperties {
	private long pollingIntervalMs = 100;
	private int maxRetries = 2;	
	private long tempoRetryMs = 100;
	private String hostname = "localhost";
	private List<DisseminationTypeConfiguration> types = new ArrayList<>();
	
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
	public List<DisseminationTypeConfiguration> getTypes() {
		return types;
	}
	public void setTypes(List<DisseminationTypeConfiguration> types) {
		this.types = types;
	}
	
	@Override
	public String toString() {
		return "DisseminationProperties [pollingIntervalMs=" + pollingIntervalMs + ", maxRetries=" + maxRetries
				+ ", tempoRetryMs=" + tempoRetryMs + ", hostname=" + hostname + ", types=" + types + "]";
	}
}