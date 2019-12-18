package esa.s1pdgs.cpoc.report.message.output;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class FilenameReportingOutput implements ReportingOutput {	
	
	@JsonProperty("filename_strings")
	private List<String> filenames;
	
	public FilenameReportingOutput(List<String> filenames) {
		this.filenames = filenames;
	}
	
	public FilenameReportingOutput(String ... filenames) {
		this(Arrays.asList(filenames));
	}
	
	public FilenameReportingOutput() {
		this(Collections.emptyList());
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}
}
