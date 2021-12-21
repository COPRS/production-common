package de.werum.coprs.nativeapi;

import java.io.IOException;
import java.net.HttpURLConnection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

/**
 * Reference System Native API Entrypoint
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
@ComponentScan({ "de.werum.coprs.nativeapi", "esa.s1pdgs.cpoc.prip", "esa.s1pdgs.cpoc.obs_sdk" })
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
				// don't follow redirects
				HttpURLConnection.setFollowRedirects(false);
				connection.setInstanceFollowRedirects(false);
			}
		});
	}

}
