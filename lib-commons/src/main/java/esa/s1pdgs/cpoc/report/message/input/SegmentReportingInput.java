package esa.s1pdgs.cpoc.report.message.input;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public class SegmentReportingInput implements ReportingInput {	
	
	@JsonProperty("segment_strings")
	private List<String> filenames;
	
	public SegmentReportingInput(final List<String> filenames) {
		this.filenames = filenames;
	}
	
	public SegmentReportingInput(final String ... filenames) {
		this(Arrays.asList(filenames));
	}	
	
	public SegmentReportingInput() {
		this(Collections.emptyList());
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public void setFilenames(final List<String> filenames) {
		this.filenames = filenames;
	}
}
