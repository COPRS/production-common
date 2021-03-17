package esa.s1pdgs.cpoc.datalifecycle.trigger.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;

@Configuration
@ConfigurationProperties("data-lifecycle-trigger")
public class DataLifecycleTriggerConfigurationProperties {
	
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
	
	public static class RetentionPolicy {

		private String productFamily;
		private String filePattern;
		private int retentionTimeDays = -1;

		public String getProductFamily() {
			return productFamily;
		}

		public void setProductFamily(String productFamily) {
			this.productFamily = productFamily;
		}

		public String getFilePattern() {
			return filePattern;
		}

		public void setFilePattern(String filePattern) {
			this.filePattern = filePattern;
		}

		public int getRetentionTimeDays() {
			return this.retentionTimeDays;
		}

		public void setRetentionTimeDays(int retentionTimeDays) {
			this.retentionTimeDays = retentionTimeDays;
		}

		@Override
		public String toString() {
			return String.format("RetentionPolicy [productFamily=%s, filePattern=%s, retentionTimeDays=%s]",
					productFamily, filePattern, retentionTimeDays);
		}
	}
	
	// --------------------------------------------------------------------------

	private Map<ProductCategory, CategoryConfig> productCategories = new LinkedHashMap<>();
	
	private List<RetentionPolicy> retentionPolicies = new ArrayList<>();
	
	// regular expressions in Java format
	private String patternPersistentInUncompressedStorage;
	private String patternPersistentInCompressedStorage;
	private String patternAvailableInLta;
	
	private String evictionTopic;
	private String dataRequestTopic;
	
	private long dataRequestCooldownInSec = 1200;
	
	// --------------------------------------------------------------------------
	
	@Override
	public String toString() {
		return String.format("DataLifecycleTriggerConfigurationProperties [productCategories=%s, retentionPolicies=%s]", productCategories, retentionPolicies);
	}
	
	// --------------------------------------------------------------------------

	public Map<ProductCategory, CategoryConfig> getProductCategories() {
		return productCategories;
	}

	public void setProductCategories(final Map<ProductCategory, CategoryConfig> productCategories) {
		this.productCategories = productCategories;
	}
	
	public List<RetentionPolicy> getRetentionPolicies() {
		return retentionPolicies;
	}

	public void setRetentionPolicies(List<RetentionPolicy> retentionPolicies) {
		this.retentionPolicies = retentionPolicies;
	}

	public String getPatternPersistentInUncompressedStorage() {
		return this.patternPersistentInUncompressedStorage;
	}

	public void setPatternPersistentInUncompressedStorage(String patternPersistentInUncompressedStorage) {
		this.patternPersistentInUncompressedStorage = patternPersistentInUncompressedStorage;
	}

	public String getPatternPersistentInCompressedStorage() {
		return this.patternPersistentInCompressedStorage;
	}

	public void setPatternPersistentInCompressedStorage(String patternPersistentInCompressedStorage) {
		this.patternPersistentInCompressedStorage = patternPersistentInCompressedStorage;
	}

	public String getPatternAvailableInLta() {
		return this.patternAvailableInLta;
	}

	public void setPatternAvailableInLta(String patternAvailableInLta) {
		this.patternAvailableInLta = patternAvailableInLta;
	}

	public String getEvictionTopic() {
		return this.evictionTopic;
	}

	public void setEvictionTopic(String evictionTopic) {
		this.evictionTopic = evictionTopic;
	}

	public String getDataRequestTopic() {
		return this.dataRequestTopic;
	}

	public void setDataRequestTopic(String dataRequestTopic) {
		this.dataRequestTopic = dataRequestTopic;
	}

	public long getDataRequestCooldownInSec() {
		return this.dataRequestCooldownInSec;
	}

	public void setDataRequestCooldownInSec(long dataRequestCooldownInSec) {
		this.dataRequestCooldownInSec = dataRequestCooldownInSec;
	}
	
}
