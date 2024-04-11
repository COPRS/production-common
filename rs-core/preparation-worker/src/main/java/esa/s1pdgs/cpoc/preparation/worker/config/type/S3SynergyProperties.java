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

package esa.s1pdgs.cpoc.preparation.worker.config.type;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "s3-synergy")
public class S3SynergyProperties {

	/**
	 * map for dynamic process parameters which are not part of the metadata (ex.
	 * facilityName)
	 */
	private Map<String, String> dynProcParams = new HashMap<>();

	public Map<String, String> getDynProcParams() {
		return dynProcParams;
	}

	public void setDynProcParams(Map<String, String> dynProcParams) {
		this.dynProcParams = dynProcParams;
	}
}
