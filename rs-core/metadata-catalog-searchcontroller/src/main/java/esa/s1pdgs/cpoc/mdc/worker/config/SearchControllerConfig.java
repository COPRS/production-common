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

package esa.s1pdgs.cpoc.mdc.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("searchcontroller")
public class SearchControllerConfig {
	// Regular Expression used to determinate if the file is an aux file. Required for correct query of aux files
	private String auxPatternConfig;

	public String getAuxPatternConfig() {
		return auxPatternConfig;
	}

	public void setAuxPatternConfig(String auxPatternConfig) {
		this.auxPatternConfig = auxPatternConfig;
	}
	
	
}
