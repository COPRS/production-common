package esa.s1pdgs.cpoc.report;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.message.input.SegmentReportingInput;

public final class ReportingUtils {
	public static final Reporting.Builder newReportingBuilder() {
		return new ReportAdapter.Builder(new LoggerReportingAppender());
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ProductFamily family, final String filename) {
		if (family == ProductFamily.L0_SEGMENT) {
			return new SegmentReportingInput(filename);
		}
		return new FilenameReportingInput(filename);
	}
}
