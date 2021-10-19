package esa.s1pdgs.cpoc.validation.config;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import esa.s1pdgs.cpoc.common.ProductFamily;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "validation")
public class ApplicationProperties {
	private Map <ProductFamily,FamilyIntervalConf> families = new LinkedHashMap<>();

	public void setFamilies(Map<ProductFamily, FamilyIntervalConf> families) {
		this.families = families;
	}

	public static class FamilyIntervalConf {
		private long initialDelay;
		private long lifeTime;

		public long getInitialDelay() {
			return initialDelay;
		}
		public void setInitialDelay(long initialDelay) {
			this.initialDelay = initialDelay;
		}
		public long getLifeTime() {
			return lifeTime;
		}
		public void setLifeTime(long lifeTime) {
			this.lifeTime = lifeTime;
		}
		
		@Override
		public String toString() {
			return "FamilyTypeConf [initialDelay=" + initialDelay +", lifeTime="+lifeTime+"]";
		}
	}

	public Map<ProductFamily, FamilyIntervalConf> getFamilies() {
		return families;
	}
	
	
}
