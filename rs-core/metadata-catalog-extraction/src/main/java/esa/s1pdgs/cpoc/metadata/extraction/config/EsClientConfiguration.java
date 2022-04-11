package esa.s1pdgs.cpoc.metadata.extraction.config;

import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "elasticsearch")
public class EsClientConfiguration {

	private String host;

	private int port;
	
    private int connectTimeoutMs;
	
    private int socketTimeoutMs;
	
	@Bean(destroyMethod = "close")
	RestHighLevelClient restHighLevelClient() throws UnknownHostException {
		HttpHost host1 = new HttpHost(host, port, "http");
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getConnectTimeoutMs() {
		return connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getSocketTimeoutMs() {
		return socketTimeoutMs;
	}

	public void setSocketTimeoutMs(int socketTimeoutMs) {
		this.socketTimeoutMs = socketTimeoutMs;
	}

}
