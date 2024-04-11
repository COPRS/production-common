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

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;

@TestConfiguration
@PropertySource("classpath:stream-parameters.properties")
public class TestConfig {

	// import environment proxy settings for downloading embedded mongodb-*.tgz
	{
		for (String protocol : List.of("http", "https")) {
			String value = System.getenv(protocol + "_proxy");
			if (null != value) {
				System.setProperty(protocol + ".proxyHost",
						value.substring(value.indexOf("://") + 3, value.lastIndexOf(":")));			
				System.setProperty(protocol + ".proxyPort",
						value.substring(value.lastIndexOf(":") + 1).replace("/", ""));			
			}
		}	
	}

}
