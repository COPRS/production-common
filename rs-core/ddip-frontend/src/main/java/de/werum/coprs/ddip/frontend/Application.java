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

package de.werum.coprs.ddip.frontend;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = "de.werum.coprs.ddip.frontend")
@EnableConfigurationProperties
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate(new SimpleClientHttpRequestFactory() {
			@Override
			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
				super.prepareConnection(connection, httpMethod);
				// don't follow redirects, instead pass them to the caller to keep consistency to direct requests to the PRIP
				HttpURLConnection.setFollowRedirects(false);
				connection.setInstanceFollowRedirects(false);
			}
		});
	}

}
