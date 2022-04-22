package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.util.HashMap;
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
		private String localDirectory;
		private String patternConfig;
		private String pathPattern = null;
		private boolean enableExtractionFromProductName = false;
		private Map<String,Integer> pathMetadataElements = new HashMap<>();

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

		public String getPathPattern() {
			return pathPattern;
		}

		public void setPathPattern(final String pathPattern) {
			this.pathPattern = pathPattern;
		}

		public boolean getEnableExtractionFromProductName() {
			return enableExtractionFromProductName;
		}

		public void setEnableExtractionFromProductname(boolean enableExtractionFromProductName) {
			this.enableExtractionFromProductName = enableExtractionFromProductName;
		}

		public Map<String, Integer> getPathMetadataElements() {
			return pathMetadataElements;
		}

		public void setPathMetadataElements(final Map<String, Integer> pathMetadataElements) {
			this.pathMetadataElements = pathMetadataElements;
		}

		@Override
		public String toString() {
			return "CategoryConfig [localDirectory=" + localDirectory + ", patternConfig=" + patternConfig + ", pathPattern="
					+ pathPattern + ", pathMetadataElements=" + pathMetadataElements + ", enableExtractionFromProductName" + enableExtractionFromProductName + "]";
		}
	}
	
	public static class ProductInsertionConfig {
		private int maxRetries;
		private int tempoRetryMs;
		
		public int getMaxRetries() {
			return maxRetries;
		}
		
		public void setMaxRetries(int maxRetries) {
			this.maxRetries = maxRetries;
		}

		public int getTempoRetryMs() {
			return tempoRetryMs;
		}
		
		public void setTempoRetryMs(int tempoRetryMs) {
			this.tempoRetryMs = tempoRetryMs;
		}
	}

	private Map<ProductCategory, CategoryConfig> productCategories = new LinkedHashMap<>();
	private ProductInsertionConfig productInsertion = new ProductInsertionConfig(); 
	
	public Map<ProductCategory, CategoryConfig> getProductCategories() {
		return productCategories;
	}

	public void setProductCategories(final Map<ProductCategory, CategoryConfig> productCategories) {
		this.productCategories = productCategories;
	}

	public ProductInsertionConfig getProductInsertion() {
		return productInsertion;
	}

	public void setProductInsertion(ProductInsertionConfig productInsertion) {
		this.productInsertion = productInsertion;
	}
	
}
