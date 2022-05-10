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
