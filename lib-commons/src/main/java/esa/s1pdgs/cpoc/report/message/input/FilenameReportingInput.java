package esa.s1pdgs.cpoc.report.message.input;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public class FilenameReportingInput implements ReportingInput {	
	
	@JsonProperty("filename_strings")
	private List<String> filenames;
	
	public FilenameReportingInput(List<String> filenames) {
		this.filenames = filenames;
	}
	
	public FilenameReportingInput(String ... filenames) {
		this(Arrays.asList(filenames));
	}	
	
	public FilenameReportingInput() {
		this(Collections.emptyList());
	}

	public List<String> getFilenames() {
		return filenames;
	}

	public void setFilenames(List<String> filenames) {
		this.filenames = filenames;
	}
}
