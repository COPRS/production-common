package esa.s1pdgs.cpoc.datalifecycle.client.config;

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
public class DlmEsClientConfiguration {

	@Value("${elasticsearch.host}")
	private String esHost;

	@Value("${elasticsearch.port}")
	private int esPort;
	
	@Value("${elasticsearch.search-result-limit}")
	private int esSearchResultLimit = 1000;
	
	@Value("${elasticsearch.connect-timeout-ms}")
    private int connectTimeoutMs;
	
	@Value("${elasticsearch.socket-timeout-ms}")
    private int socketTimeoutMs;
	
	private final String esIndexName = "data-lifecycle-metadata";
	
	// --------------------------------------------------------------------------
	
	@Bean(name = "dlmEsClient", destroyMethod = "close")
	RestHighLevelClient restHighLevelClient() throws UnknownHostException {
		final HttpHost host = new HttpHost(this.esHost, this.esPort, "http");
		final RestClientBuilder builder = RestClient.builder(host)
				.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
					@Override
					public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
						return requestConfigBuilder.setConnectTimeout(DlmEsClientConfiguration.this.connectTimeoutMs)
								.setSocketTimeout(DlmEsClientConfiguration.this.socketTimeoutMs);
					}
				});
		
		return new RestHighLevelClient(builder);
	}
	
	// --------------------------------------------------------------------------

	public String getEsHost() {
		return this.esHost;
	}

	public void setEsHost(String esHost) {
		this.esHost = esHost;
	}

	public int getEsPort() {
		return this.esPort;
	}

	public void setEsPort(int esPort) {
		this.esPort = esPort;
	}

	public int getConnectTimeoutMs() {
		return this.connectTimeoutMs;
	}

	public void setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
	}

	public int getSocketTimeoutMs() {
		return this.socketTimeoutMs;
	}

	public void setSocketTimeoutMs(int socketTimeoutMs) {
		this.socketTimeoutMs = socketTimeoutMs;
	}

	public int getEsSearchResultLimit() {
		return this.esSearchResultLimit;
	}

	public void setEsSearchResultLimit(int esSearchResultLimit) {
		this.esSearchResultLimit = esSearchResultLimit;
	}
	
	public String getEsIndexName() {
		return this.esIndexName;
	}

}
