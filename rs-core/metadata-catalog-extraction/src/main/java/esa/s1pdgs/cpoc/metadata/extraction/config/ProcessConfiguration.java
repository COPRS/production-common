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

package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.util.Collections;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ProcessConfiguration {
	private String hostname = "";
	private int numObsDownloadRetries = 99;
	private long sleepBetweenObsRetriesMillis = 3000L;

	/*
	 * Determine manifest filename by filename extension. The placeholder
	 * "<PRODUCTNAME>" will be replaced by the actual product key 
	 * 
	 * ex.
	 *   <PRODUCTNAME>_iif.xml for product S3A_SL_0_SR___G will be converted to
	 *   S3A_SL_0_SR___G_iif.xml
	 */
	private Map<String, String> manifestFilenames = Collections.singletonMap("safe", "manifest.safe");

	public String getHostname() {
		return hostname;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public int getNumObsDownloadRetries() {
		return numObsDownloadRetries;
	}

	public void setNumObsDownloadRetries(final int numObsDownloadRetries) {
		this.numObsDownloadRetries = numObsDownloadRetries;
	}

	public long getSleepBetweenObsRetriesMillis() {
		return sleepBetweenObsRetriesMillis;
	}

	public void setSleepBetweenObsRetriesMillis(final long sleepBetweenObsRetriesMillis) {
		this.sleepBetweenObsRetriesMillis = sleepBetweenObsRetriesMillis;
	}

	public Map<String, String> getManifestFilenames() {
		return manifestFilenames;
	}

	public void setManifestFilenames(Map<String, String> manifestFilenames) {
		this.manifestFilenames = manifestFilenames;
	}
}
