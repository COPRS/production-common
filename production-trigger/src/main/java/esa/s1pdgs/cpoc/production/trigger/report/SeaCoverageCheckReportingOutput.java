package esa.s1pdgs.cpoc.production.trigger.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class SeaCoverageCheckReportingOutput extends FilenameReportingOutput{
	@JsonProperty("over_sea_boolean")
	private boolean isOverSea;

	public SeaCoverageCheckReportingOutput(final String filename, final boolean isOverSea) {
		super(filename);
		this.isOverSea = isOverSea;
	}

	public boolean isOverSea() {
		return isOverSea;
	}

	public void setOverSea(final boolean isOverSea) {
		this.isOverSea = isOverSea;
	}
}
