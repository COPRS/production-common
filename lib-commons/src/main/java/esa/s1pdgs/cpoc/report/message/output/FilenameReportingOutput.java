package esa.s1pdgs.cpoc.report.message.output;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class FilenameReportingOutput  extends AbstractFilenameReportingProduct implements ReportingOutput {
	public FilenameReportingOutput(final ReportingFilenameEntries entries) {
		super(entries);
	}
}
