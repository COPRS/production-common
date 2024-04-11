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

package de.werum.coprs.requestparkinglot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestParkingLotConfiguration {
	private final String defaultResubmitTopic;

	public RequestParkingLotConfiguration(
			@Value("${defaultResubmitTopic:catalog-event}") final String defaultResubmitTopic
    ) {
		this.defaultResubmitTopic = defaultResubmitTopic;
	}
	
	public String getDefaultResubmitTopic() {
		return defaultResubmitTopic;
	}
}
