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

package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "ingestion-trigger")
public class IngestionTriggerConfigurationProperties {
	
	private Map<String, InboxConfiguration> polling = new HashMap<>();

	public Map<String, InboxConfiguration> getPolling() {
		return polling;
	}

	public void setPolling(Map<String, InboxConfiguration> polling) {
		this.polling = polling;
	}

	@Override
	public String toString() {
		return "InboxPollingConfigurationProperties [polling=" + polling + "]";
	}

}
