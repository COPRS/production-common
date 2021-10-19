package esa.s1pdgs.cpoc.prip.frontend.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class PripReportingOutput implements ReportingOutput {

	@JsonProperty("download_url_string")
	private String downloadUrl;

	public PripReportingOutput() {
		
	}
	
	public PripReportingOutput(final String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}

	public String getDownloadUrl() {
		return downloadUrl;
	}

	public void setDownloadUrl(final String downloadUrl) {
		this.downloadUrl = downloadUrl;
	}
}
