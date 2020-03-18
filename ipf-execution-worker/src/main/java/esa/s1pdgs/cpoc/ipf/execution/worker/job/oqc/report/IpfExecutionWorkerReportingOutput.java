package esa.s1pdgs.cpoc.ipf.execution.worker.job.oqc.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IpfExecutionWorkerReportingOutput extends FilenameReportingOutput {
	public static class Segment {
		@JsonProperty("filename_string")
		private String segmentName;
		
		public Segment(final String segmentName) {
			this.segmentName = segmentName;
		}

		public String getSegmentName() {
			return segmentName;
		}

		public void setSegmentName(final String segmentName) {
			this.segmentName = segmentName;
		}
	}
	
	@JsonProperty("segment_objects")
	private List<Segment> segments;

	public IpfExecutionWorkerReportingOutput(final List<String> filenames, final List<Segment> segments) {
		super(filenames);
		this.segments = segments;
	}

	public List<Segment> getSegments() {
		return segments;
	}

	public void setSegments(final List<Segment> segments) {
		this.segments = segments;
	}
}
