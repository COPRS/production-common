package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OutboxReportingOutput implements ReportingOutput {	
	@JsonProperty("outbox_url_string")
	private String url;
	
	public OutboxReportingOutput(String url) {
		this.url = url;
	}
	
	public OutboxReportingOutput() {
		this(null);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
