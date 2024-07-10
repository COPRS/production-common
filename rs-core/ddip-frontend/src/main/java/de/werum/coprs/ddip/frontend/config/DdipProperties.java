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

package de.werum.coprs.ddip.frontend.config;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ddip")
public class DdipProperties {

	private String majorVersion;
	private String version;

	private String dispatchPripProtocol;
	private String dispatchPripHost;
	private Integer dispatchPripPort;

	private Map<String, String> collections;

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

	public String getDispatchPripProtocol() {
		return this.dispatchPripProtocol;
	}

	public void setDispatchPripProtocol(String dispatchPripProtocol) {
		this.dispatchPripProtocol = dispatchPripProtocol;
	}

	public String getDispatchPripHost() {
		return this.dispatchPripHost;
	}

	public void setDispatchPripHost(String dispatchPripHost) {
		this.dispatchPripHost = dispatchPripHost;
	}

	public Integer getDispatchPripPort() {
		return this.dispatchPripPort;
	}

	public void setDispatchPripPort(Integer dispatchPripPort) {
		this.dispatchPripPort = dispatchPripPort;
	}

	public Map<String, String> getCollections() {
		return this.collections;
	}

	public void setCollections(Map<String, String> collections) {
		this.collections = collections;
	}

}
