package esa.s1pdgs.cpoc.ingestion.filter.config;

import java.util.Map;

import org.apache.logging.log4j.core.util.CronExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.metadata.model.MissionId;

@Configuration
@ConfigurationProperties("ingestion-filter")
public class IngestionFilterConfigurationProperties {

	public static class FilterProperties {		
		// Cronjob definition for products, that should be processed
		private String cronDefinition;

		public String getCronDefinition() {
			return cronDefinition;
		}

		public void setCronDefinition(String cronDefinition) {
			this.cronDefinition = cronDefinition;
		}
	}
	
	private long pollingIntervalMs = 1000;
	private long pollingInitialDelayMs = 5000;
	
	private Map<MissionId, FilterProperties> config;
	
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

	public Map<MissionId, FilterProperties> getConfig() {
		return config;
	}

	public void setConfig(Map<MissionId, FilterProperties> config) {
		this.config = config;
	}
}
