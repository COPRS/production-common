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

package esa.s1pdgs.cpoc.ingestion.worker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("ingestion-worker")
public class IngestionWorkerServiceConfigurationProperties {	
	private long pollingIntervalMs = 100;
	private int maxRetries = 2;	
	private long tempoRetryMs = 100;
	private String hostname = "localhost";
	private boolean bufferInputs = false;
	
	public long getPollingIntervalMs() {
		return pollingIntervalMs;
	}
	
	public void setPollingIntervalMs(final long pollingIntervalMs) {
		this.pollingIntervalMs = pollingIntervalMs;
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}
	
	public int getMaxRetries() {
		return maxRetries;
	}

	public void setMaxRetries(final int maxRetries) {
		this.maxRetries = maxRetries;
	}

	public long getTempoRetryMs() {
		return tempoRetryMs;
	}

	public void setTempoRetryMs(final long tempoRetryMs) {
		this.tempoRetryMs = tempoRetryMs;
	}

	public boolean isBufferInputs() {
		return bufferInputs;
	}

	public void setBufferInputs(final boolean bufferInputs) {
		this.bufferInputs = bufferInputs;
	}	
}
