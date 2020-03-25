package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IpfExecutionWorkerReportingOutput extends FilenameReportingOutput {	
	@JsonProperty("segment_objects")
	private List<String> segments;

	public IpfExecutionWorkerReportingOutput(final List<String> filenames, final List<String> segments) {
		super(filenames);
		this.segments = segments;
	}

	public List<String> getSegments() {
		return segments;
	}

	public void setSegments(final List<String> segments) {
		this.segments = segments;
	}
}
