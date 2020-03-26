package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class GhostHandlingSegmentReportingOutput implements ReportingOutput {
	@JsonProperty("is_ghost_candidate_boolean")
	private boolean isGhostCandidate;

	public GhostHandlingSegmentReportingOutput(final boolean isGhostCandidate) {
		this.isGhostCandidate = isGhostCandidate;
	}

	public boolean isGhostCandidate() {
		return isGhostCandidate;
	}

	public void setGhostCandidate(final boolean isGhostCandidate) {
		this.isGhostCandidate = isGhostCandidate;
	}	
}
