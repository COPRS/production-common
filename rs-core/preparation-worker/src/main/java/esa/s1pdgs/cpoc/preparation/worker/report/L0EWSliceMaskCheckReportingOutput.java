package esa.s1pdgs.cpoc.preparation.worker.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class L0EWSliceMaskCheckReportingOutput implements ReportingOutput {
	
	@JsonProperty("intersects_mask_boolean")
	private boolean intersectsMask;
	
	public L0EWSliceMaskCheckReportingOutput(final boolean intersectsMask) {
		this.intersectsMask = intersectsMask;
	}
	
	public boolean getIntersectsMask() {
		return this.intersectsMask;
	}
	
	public void setIntersectsMask(final boolean intersectsMask) {
		this.intersectsMask = intersectsMask;
	}
	

}
