package esa.s1pdgs.cpoc.mdc.worker.config;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;

@Configuration
@ConfigurationProperties("worker")
public class MdcWorkerConfigurationProperties {
	public static class FamilyConfig
	{
		private long fixedDelayMs = 500L;
		private long initDelayPollMs = 2000L;
		private String localDirectory;
		private String patternConfig;
		private String pathPattern = null;
		private Map<String,Integer> pathMetadataElements = new HashMap<>();

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
		
		public String getPathPattern() {
			return pathPattern;
		}

		public void setPathPattern(final String pathPattern) {
			this.pathPattern = pathPattern;
		}

		public Map<String, Integer> getPathMetadataElements() {
			return pathMetadataElements;
		}

		public void setPathMetadataElements(final Map<String, Integer> pathMetadataElements) {
			this.pathMetadataElements = pathMetadataElements;
		}

		@Override
		public String toString() {
			return "FamilyConfig [fixedDelayMs=" + fixedDelayMs + ", initDelayPollMs=" + initDelayPollMs
					+ ", localDirectory=" + localDirectory + ", patternConfig=" + patternConfig + ", pathPattern="
					+ pathPattern + ", pathMetadataElements=" + pathMetadataElements + "]";
		}
	}

	private Map<ProductFamily, FamilyConfig> productFamilies= new LinkedHashMap<>();
	
	public Map<ProductFamily, FamilyConfig> getProductFamilies() {
		return productFamilies;
	}

	public void setProductFamilies(final Map<ProductFamily, FamilyConfig> productFamilies) {
		this.productFamilies = productFamilies;
	}
}
