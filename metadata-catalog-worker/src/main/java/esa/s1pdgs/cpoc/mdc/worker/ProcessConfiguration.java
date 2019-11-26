package esa.s1pdgs.cpoc.mdc.worker;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "process")
public class ProcessConfiguration {

	private String hostname = "";
	private int numObsDownloadRetries = 99;
	private long sleepBetweenObsRetriesMillis = 3000L; 

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public int getNumObsDownloadRetries() {
		return numObsDownloadRetries;
	}

	public void setNumObsDownloadRetries(int numObsDownloadRetries) {
		this.numObsDownloadRetries = numObsDownloadRetries;
	}

	public long getSleepBetweenObsRetriesMillis() {
		return sleepBetweenObsRetriesMillis;
	}

	public void setSleepBetweenObsRetriesMillis(long sleepBetweenObsRetriesMillis) {
		this.sleepBetweenObsRetriesMillis = sleepBetweenObsRetriesMillis;
	}
}
