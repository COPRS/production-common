package esa.s1pdgs.cpoc.ingestion.trigger.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:stream-parameters.properties")
public class Config {

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
