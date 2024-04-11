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

	public Map<String, MetadataMapping> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, MetadataMapping> metadata) {
		this.metadata = metadata;
	}

}
