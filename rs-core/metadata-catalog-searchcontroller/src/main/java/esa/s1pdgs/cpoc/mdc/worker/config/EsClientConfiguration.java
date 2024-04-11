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

import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EsClientConfiguration {

	@Value("${elasticsearch.host}")
	private String esHost;

	@Value("${elasticsearch.port}")
	private int esPort;
	
	@Value("${elasticsearch.connect-timeout-ms}")
    private int connectTimeoutMs;
	
	@Value("${elasticsearch.socket-timeout-ms}")
    private int socketTimeoutMs;
	
	@Bean(destroyMethod = "close")
	RestHighLevelClient restHighLevelClient() throws UnknownHostException {
		HttpHost host1 = new HttpHost(esHost, esPort, "http");
		RestClientBuilder builder = RestClient.builder(host1)
		        .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig
                    (RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(connectTimeoutMs)
                        .setSocketTimeout(socketTimeoutMs);
            }
        });
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}

}
