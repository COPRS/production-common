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

		public void setEnableExtractionFromProductName(boolean enableExtractionFromProductName) {
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
