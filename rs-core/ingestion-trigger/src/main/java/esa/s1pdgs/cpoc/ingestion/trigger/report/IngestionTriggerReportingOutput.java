package esa.s1pdgs.cpoc.ingestion.trigger.report;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class IngestionTriggerReportingOutput implements ReportingOutput {
	private String url;
	
	public IngestionTriggerReportingOutput() {
	}

	public IngestionTriggerReportingOutput(final String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}
}
