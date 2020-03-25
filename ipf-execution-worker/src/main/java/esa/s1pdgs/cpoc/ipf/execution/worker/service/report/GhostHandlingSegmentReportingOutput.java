package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.output.SegmentReportingOutput;

public class GhostHandlingSegmentReportingOutput extends SegmentReportingOutput {
	@JsonProperty("is_ghost_candidate_boolean")
	private boolean isGhostCandidate;

	public GhostHandlingSegmentReportingOutput(final String filename, final boolean isGhostCandidate) {
		super(filename);
		this.isGhostCandidate = isGhostCandidate;
	}

	public boolean isGhostCandidate() {
		return isGhostCandidate;
	}

	public void setGhostCandidate(final boolean isGhostCandidate) {
		this.isGhostCandidate = isGhostCandidate;
	}	
}
