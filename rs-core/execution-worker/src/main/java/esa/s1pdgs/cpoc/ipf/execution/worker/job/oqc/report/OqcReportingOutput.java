package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class OqcReportingOutput implements ReportingOutput {
	
	@JsonProperty("oqc_result_string")
	private String oqcFlagContent;

	public OqcReportingOutput() {
		
	}
	public OqcReportingOutput(final String oqcFlagContent) {
		this.oqcFlagContent = oqcFlagContent;
	}

	public String getOqcFlagContent() {
		return oqcFlagContent;
	}

	public void setOqcFlagContent(final String oqcFlagContent) {
		this.oqcFlagContent = oqcFlagContent;
	}	
}
