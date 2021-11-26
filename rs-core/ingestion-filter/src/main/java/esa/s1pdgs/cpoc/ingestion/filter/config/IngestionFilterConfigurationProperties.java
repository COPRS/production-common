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
		// Used to define groups within the product name
		private String nameRegex;
		
		// Index of group that should be evaluated as timestamp in this filter
		private int groupIdx;
		
		// Cronjob definition for products, that should be processed
		private CronExpression cronDefinition;

		public String getNameRegex() {
			return nameRegex;
		}

		public void setNameRegex(String nameRegex) {
			this.nameRegex = nameRegex;
		}

		public int getGroupIdx() {
			return groupIdx;
		}

		public void setGroupIdx(int groupIdx) {
			this.groupIdx = groupIdx;
		}

		public CronExpression getCronDefinition() {
			return cronDefinition;
		}

		public void setCronDefinition(CronExpression cronDefinition) {
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
