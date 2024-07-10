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

package de.werum.coprs.nativeapi.config;

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

	private String majorVersion;
	private String version;
	private Long downloadUrlExpirationTimeInSeconds;
	private String dummyDownloadFile;

	private List<AttributesOfMission> attributesOfMission = new LinkedList<>();

	public static class AttributesOfMission {
		private String missionName;
		private List<String> baseAttributes = new LinkedList<>();
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

		public List<String> getBaseAttributes() {
			return this.baseAttributes;
		}

		public void setBaseAttributes(List<String> baseAttributes) {
			this.baseAttributes = baseAttributes;
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

	public String getMajorVersion() {
		return this.majorVersion;
	}

	public void setMajorVersion(final String majorVersion) {
		this.majorVersion = majorVersion;
	}

	public String getVersion() {
		return this.version;
	}

	public void setVersion(final String version) {
		this.version = version;
	}

	public List<AttributesOfMission> getAttributesOfMission() {
		return this.attributesOfMission;
	}

	public void setAttributesOfMission(final List<AttributesOfMission> attributesOfMission) {
		this.attributesOfMission = attributesOfMission;
	}

	public Long getDownloadUrlExpirationTimeInSeconds() {
		return this.downloadUrlExpirationTimeInSeconds;
	}

	public void setDownloadUrlExpirationTimeInSeconds(final Long downloadUrlExpirationTimeInSeconds) {
		this.downloadUrlExpirationTimeInSeconds = downloadUrlExpirationTimeInSeconds;
	}

	public String getDummyDownloadFile() {
		return this.dummyDownloadFile;
	}

	public void setDummyDownloadFile(final String dummyDownloadFile) {
		this.dummyDownloadFile = dummyDownloadFile;
	}

}
