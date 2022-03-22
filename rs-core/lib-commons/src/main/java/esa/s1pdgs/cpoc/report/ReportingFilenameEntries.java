package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.List;

public final class ReportingFilenameEntries {
	private final List<ReportingFilenameEntry> reportingEntries;

	public ReportingFilenameEntries(final ReportingFilenameEntry entry) {
		this.reportingEntries = Collections.singletonList(entry);
	}
	
	public ReportingFilenameEntries(final List<ReportingFilenameEntry> reportingEntries) {
		this.reportingEntries = reportingEntries;
	}
	
	public final List<String> getFilenames() {
		return ReportingUtils.filenamesOf(reportingEntries);
	}
	
	public final List<String> getSegments() {
		return ReportingUtils.segmentsOf(reportingEntries);
	}	
	
	final List<ReportingFilenameEntry> entries() {
		return reportingEntries;
	}
}
