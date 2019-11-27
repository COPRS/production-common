package esa.s1pdgs.cpoc.mdc.worker.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;

@Configuration
@ConfigurationProperties("worker")
public class MdcWorkerConfigurationProperties {
	public static class CategoryConfig
	{
		private long fixedDelayMs = 500L;
		private long initDelayPollMs = 2000L;
		private String localDirectory;
		private String patternConfig;

		public long getFixedDelayMs() {
			return fixedDelayMs;
		}

		public void setFixedDelayMs(final long fixedDelayMs) {
			this.fixedDelayMs = fixedDelayMs;
		}

		public long getInitDelayPollMs() {
			return initDelayPollMs;
		}

		public void setInitDelayPollMs(final long initDelayPolMs) {
			this.initDelayPollMs = initDelayPolMs;
		}

		public String getLocalDirectory() {
			return localDirectory;
		}

		public void setLocalDirectory(final String localDirectory) {
			this.localDirectory = localDirectory;
		}

		public String getPatternConfig() {
			return patternConfig;
		}

		public void setPatternConfig(final String patternConfig) {
			this.patternConfig = patternConfig;
		}

		@Override
		public String toString() {
			return "CategoryConfig [fixedDelayMs=" + fixedDelayMs + ", initDelayPollMs=" + initDelayPollMs
					+ ", localDirectory=" + localDirectory + ", patternConfig=" + patternConfig + "]";
		}
	}

	private Map<ProductCategory, CategoryConfig> productCategories = new LinkedHashMap<>();
	
	public Map<ProductCategory, CategoryConfig> getProductCategories() {
		return productCategories;
	}

	public void setProductCategories(final Map<ProductCategory, CategoryConfig> productCategories) {
		this.productCategories = productCategories;
	}
}
