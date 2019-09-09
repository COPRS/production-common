package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FilenameReportingOutput implements ReportingOutput {	
	
	@JsonProperty("filename_strings")
	private List<String> filenames;
	
	public FilenameReportingOutput(List<String> filenames) {
		this.filenames = filenames;
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
