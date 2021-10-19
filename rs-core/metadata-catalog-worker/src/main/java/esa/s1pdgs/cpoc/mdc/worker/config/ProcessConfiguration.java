package esa.s1pdgs.cpoc.mdc.worker.config;

import java.util.Collections;
import java.util.Map;

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

	/*
	 * Determine manifest filename by filename extension. The placeholder
	 * "<PRODUCTNAME>" will be replaced by the actual product key 
	 * 
	 * ex.
	 *   <PRODUCTNAME>_iif.xml for product S3A_SL_0_SR___G will be converted to
	 *   S3A_SL_0_SR___G_iif.xml
	 */
	private Map<String, String> manifestFilenames = Collections.singletonMap("safe", "manifest.safe");

	public String getHostname() {
		return hostname;
	}

	public void setHostname(final String hostname) {
		this.hostname = hostname;
	}

	public int getNumObsDownloadRetries() {
		return numObsDownloadRetries;
	}

	public void setNumObsDownloadRetries(final int numObsDownloadRetries) {
		this.numObsDownloadRetries = numObsDownloadRetries;
	}

	public long getSleepBetweenObsRetriesMillis() {
		return sleepBetweenObsRetriesMillis;
	}

	public void setSleepBetweenObsRetriesMillis(final long sleepBetweenObsRetriesMillis) {
		this.sleepBetweenObsRetriesMillis = sleepBetweenObsRetriesMillis;
	}

	public Map<String, String> getManifestFilenames() {
		return manifestFilenames;
	}

	public void setManifestFilenames(Map<String, String> manifestFilenames) {
		this.manifestFilenames = manifestFilenames;
	}
}
