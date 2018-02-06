package fr.viveris.s1pdgs.mdcatalog.config.es;

import java.net.UnknownHostException;

import org.apache.http.HttpHost;
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

	@Bean(destroyMethod = "close")
	RestHighLevelClient restHighLevelClient() throws UnknownHostException {
		HttpHost host1 = new HttpHost(esHost, esPort, "http");
		RestClientBuilder builder = RestClient.builder(host1);
		RestHighLevelClient client = new RestHighLevelClient(builder);
		return client;
	}

}
