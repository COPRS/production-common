package esa.s1pdgs.cpoc.report;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InboxReportingInput implements ReportingInput {	
	@JsonProperty("inbox_url_string")
	private String url;
	
	public InboxReportingInput(String url) {
		this.url = url;
	}
	
	public InboxReportingInput() {
		this(null);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
