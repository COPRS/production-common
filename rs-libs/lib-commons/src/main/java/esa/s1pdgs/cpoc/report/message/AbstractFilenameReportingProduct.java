package esa.s1pdgs.cpoc.report.message;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;

public abstract class AbstractFilenameReportingProduct {
	@JsonIgnore
	private final List<String> filenames;

	protected AbstractFilenameReportingProduct(final ReportingFilenameEntries entries) {
		this.filenames = entries.getFilenames();
	}

	@JsonAnyGetter
	public final Map<String, List<String>> getFilenames() {
		if (filenames == null || filenames.size() == 0) {
			return null;
		} else if (filenames.size() > 1) {
			return Collections.singletonMap("filename_strings", filenames);
		} else {
			return Collections.singletonMap("filename_string", filenames);
		}
	}
}
