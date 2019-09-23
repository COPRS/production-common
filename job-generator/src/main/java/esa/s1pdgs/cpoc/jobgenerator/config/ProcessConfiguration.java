package esa.s1pdgs.cpoc.jobgenerator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("process")
public class ProcessConfiguration {
	private String hostname;
	
	public String getHostname() {
		return hostname;
	}
	
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
}
