/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.ingestion.filter.config;

import java.util.HashMap;
import java.util.Map;

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
	
	private Map<MissionId, FilterProperties> config = new HashMap<>();
	
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
