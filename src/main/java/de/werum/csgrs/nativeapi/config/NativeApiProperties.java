package de.werum.csgrs.nativeapi.config;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("native-api")
public class NativeApiProperties {

	private String version;

	private List<AttributesOfMission> attributesOfMission = new LinkedList<>();

	public static class AttributesOfMission {
		private String missionName;
		private List<AttributesOfProductType> attributesOfProductType = new LinkedList<>();

		public String getMissionName() {
			return this.missionName;
		}

		public void setMissionName(String missionName) {
			this.missionName = missionName;
		}

		public List<AttributesOfProductType> getAttributesOfProductType() {
			return this.attributesOfProductType;
		}

		public void setAttributesOfProductType(List<AttributesOfProductType> attributesOfProductType) {
			this.attributesOfProductType = attributesOfProductType;
		}
	}

	public static class AttributesOfProductType {
		private String productType;
		private Map<String, String> attributes = new LinkedHashMap<>();

		public String getProductType() {
			return this.productType;
		}
		public void setProductType(String productType) {
			this.productType = productType;
		}

		public Map<String, String> getAttributes() {
			return this.attributes;
		}

		public void setAttributes(Map<String, String> attributes) {
			this.attributes = attributes;
		}
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<AttributesOfMission> getAttributesOfMission() {
		return this.attributesOfMission;
	}

	public void setAttributesOfMission(List<AttributesOfMission> attributesOfMission) {
		this.attributesOfMission = attributesOfMission;
	}

}
