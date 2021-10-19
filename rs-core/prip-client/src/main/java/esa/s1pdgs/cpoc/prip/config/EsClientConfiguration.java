package esa.s1pdgs.cpoc.prip.config;

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
