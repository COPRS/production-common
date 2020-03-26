package esa.s1pdgs.cpoc.production.trigger.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class SeaCoverageCheckReportingOutput implements ReportingOutput {
	@JsonProperty("over_sea_boolean")
	private boolean isOverSea;

	public SeaCoverageCheckReportingOutput(final boolean isOverSea) {
		this.isOverSea = isOverSea;
	}

	public boolean isOverSea() {
		return isOverSea;
	}

	public void setOverSea(final boolean isOverSea) {
		this.isOverSea = isOverSea;
	}
}
