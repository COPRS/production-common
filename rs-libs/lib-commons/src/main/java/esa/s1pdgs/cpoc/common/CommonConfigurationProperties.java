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

package esa.s1pdgs.cpoc.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties("common")
public class CommonConfigurationProperties {
	
	
	private String rsChainName;
	private String rsChainVersion;
	
	public String getRsChainName() {
		return rsChainName;
	}
	public void setRsChainName(String rsChainName) {
		this.rsChainName = rsChainName;
	}
	public String getRsChainVersion() {
		return rsChainVersion;
	}
	public void setRsChainVersion(String rsChainVersion) {
		this.rsChainVersion = rsChainVersion;
	}
	
	@Override
	public String toString() {
		return "CommonConfigurationProperties [rsChainName=" + rsChainName + ", rsChainVersion=" + rsChainVersion + "]";
	}
	
	

}
