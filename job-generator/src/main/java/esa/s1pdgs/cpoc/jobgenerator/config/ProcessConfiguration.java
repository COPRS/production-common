package esa.s1pdgs.cpoc.jobgenerator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("process")
@Component
public class ProcessConfiguration {
	private String hostname;
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
