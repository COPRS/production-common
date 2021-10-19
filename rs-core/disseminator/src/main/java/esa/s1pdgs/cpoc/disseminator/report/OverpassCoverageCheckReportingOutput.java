package esa.s1pdgs.cpoc.disseminator.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class OverpassCoverageCheckReportingOutput implements ReportingOutput {
	@JsonProperty("over_overpass_boolean")
	private boolean overOverpass;

	public OverpassCoverageCheckReportingOutput(final boolean overOverpass) {
		this.overOverpass = overOverpass;
	}
	
	public boolean getOverOverpass() {
		return overOverpass;
	}

	public void setOverOverpass(final boolean overOverpass) {
		this.overOverpass = overOverpass;
	}
}
