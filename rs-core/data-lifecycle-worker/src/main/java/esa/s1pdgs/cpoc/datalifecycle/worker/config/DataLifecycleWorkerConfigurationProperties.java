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

package esa.s1pdgs.cpoc.datalifecycle.worker.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.datalifecycle.client.domain.model.RetentionPolicy;

@Configuration
@ConfigurationProperties("data-lifecycle-worker")
public class DataLifecycleWorkerConfigurationProperties {
	
	public static class CategoryConfig
	{
		private long fixedDelayMs = 500L;
		private long initDelayPolMs = 2000L;

		public long getFixedDelayMs() {
			return fixedDelayMs;
		}

		public void setFixedDelayMs(final long fixedDelayMs) {
			this.fixedDelayMs = fixedDelayMs;
		}

		public long getInitDelayPolMs() {
			return initDelayPolMs;
		}

		public void setInitDelayPolMs(final long initDelayPolMs) {
			this.initDelayPolMs = initDelayPolMs;
		}

		@Override
		public String toString() {
			return "CategoryConfig [fixedDelayMs=" + fixedDelayMs + ", initDelayPolMs=" + initDelayPolMs + "]";
		}
	}
	
	private Map<String, RetentionPolicy> retentionPolicies = new LinkedHashMap<>();
	
	private Map<ProductFamily, Integer> shorteningEvictionTimeAfterCompression = new LinkedHashMap<>();
	
	
	@Override
	public String toString() {
		return String.format("DataLifecycleWorkerConfigurationProperties [retentionPolicies=%s, shorteningEvictionTimeAfterCompression=%s]", retentionPolicies, shorteningEvictionTimeAfterCompression);
	}
	
	public Map<String, RetentionPolicy> getRetentionPolicies() {
		return retentionPolicies;
	}

	public void setRetentionPolicies(Map<String, RetentionPolicy> retentionPolicies) {
		this.retentionPolicies = retentionPolicies;
	}
	
	public Map<ProductFamily, Integer> getShorteningEvictionTimeAfterCompression() {
		return shorteningEvictionTimeAfterCompression;
	}

	public void setShorteningEvictionTimeAfterCompression(Map<ProductFamily, Integer> shortingEvictionTimeAfterCompression) {
		this.shorteningEvictionTimeAfterCompression = shortingEvictionTimeAfterCompression;
	}

}
