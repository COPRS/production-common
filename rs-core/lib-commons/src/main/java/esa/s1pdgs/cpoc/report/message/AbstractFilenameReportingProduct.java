package esa.s1pdgs.cpoc.report.message;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;

public abstract class AbstractFilenameReportingProduct {
	@JsonProperty("filename_strings")
	@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
	private final List<String> filenames;
	
	@JsonProperty("segment_strings")
	@JsonInclude(value=Include.NON_EMPTY, content=Include.NON_NULL)
	private final List<String> segments;
	
	protected AbstractFilenameReportingProduct(final ReportingFilenameEntries entries) {
		this.filenames = entries.getFilenames();
		this.segments = entries.getSegments();
	}

	public final List<String> getFilenames() {
		return filenames;
	}

	public final List<String> getSegments() {
		return segments;
	}
}
