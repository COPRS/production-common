package esa.s1pdgs.cpoc.production.trigger.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class SeaCoverageCheckReportingOutput implements ReportingOutput {
	@JsonProperty("over_sea_boolean")
	private boolean overSea;

	public SeaCoverageCheckReportingOutput(final boolean overSea) {
		this.overSea = overSea;
	}
	
	public boolean getOverSea() {
		return overSea;
	}

	public void setOverSea(final boolean overSea) {
		this.overSea = overSea;
	}
}
