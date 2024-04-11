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

package esa.s1pdgs.cpoc.datarequest.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("data-request-worker")
public class WorkerConfigurationProperties {
	
	private long pollingIntervalMs = 1000;
	private long pollingInitialDelayMs = 5000;
	private String hostname;
	
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}
	public void setPollingIntervalMs(final long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	public long getPollingInitialDelayMs() {
		return pollingInitialDelayMs;
	}
	public void setPollingInitialDelayMs(final long pollingInitialDelayMs) {
		this.pollingInitialDelayMs = pollingInitialDelayMs;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

}
