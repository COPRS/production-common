package esa.s1pdgs.cpoc.mdc.worker.extraction.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class TimelinessReportingOutput implements ReportingOutput {	
	@JsonProperty("timeliness_string")
	private String timeliness;
	
	public TimelinessReportingOutput() {
		
	}
	
	public TimelinessReportingOutput(final String timeliness) {
		this.timeliness = timeliness;
	}

	public String getTimeliness() {
		return timeliness;
	}

	public void setTimeliness(final String timeliness) {
		this.timeliness = timeliness;
	}
}
