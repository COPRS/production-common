package esa.s1pdgs.cpoc.report.message.output;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class SegmentReportingOutput implements ReportingOutput {	
	@JsonProperty("segment_strings")
	private List<String> filenames;
	
	public SegmentReportingOutput(final List<String> filenames) {
		this.filenames = filenames;
	}
	
	public SegmentReportingOutput(final String ... filenames) {
		this(Arrays.asList(filenames));
	}
	
	public SegmentReportingOutput() {
		this(Collections.emptyList());
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public void setFilenames(final List<String> filenames) {
		this.filenames = filenames;
	}
}
