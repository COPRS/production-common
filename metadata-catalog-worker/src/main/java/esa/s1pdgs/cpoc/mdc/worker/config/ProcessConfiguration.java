package esa.s1pdgs.cpoc.mdc.worker.config;

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
	private String fileWithManifestExt = ".safe";
	private String manifestFilename = "manifest.safe";  
	

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
	
	public String getFileWithManifestExt() {
		return fileWithManifestExt;
	}

	public void setFileWithManifestExt(final String fileWithManifestExt) {
		this.fileWithManifestExt = fileWithManifestExt;
	}

	public String getManifestFilename() {
		return manifestFilename;
	}

	public void setManifestFilename(final String manifestFilename) {
		this.manifestFilename = manifestFilename;
	}
}
