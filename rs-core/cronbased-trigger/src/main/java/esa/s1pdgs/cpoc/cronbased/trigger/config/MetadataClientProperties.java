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

package esa.s1pdgs.cpoc.cronbased.trigger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "metadata")
public class MetadataClientProperties {
	/**
	 * Host URI for the applicative catalog server
	 */
	private String metadataHostname;

	/**
	 * Maximal number of retries when query fails
	 */
	private int nbretry;

	/**
	 * Temporisation in ms between 2 retries
	 */
	private int temporetryms;

	public String getMetadataHostname() {
		return metadataHostname;
	}

	public void setMetadataHostname(String metadataHostname) {
		this.metadataHostname = metadataHostname;
	}

	public int getNbretry() {
		return nbretry;
	}

	public void setNbretry(int nbretry) {
		this.nbretry = nbretry;
	}

	public int getTemporetryms() {
		return temporetryms;
	}

	public void setTemporetryms(int temporetryms) {
		this.temporetryms = temporetryms;
	}
}
