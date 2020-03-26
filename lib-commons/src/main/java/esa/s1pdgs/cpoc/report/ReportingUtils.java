package esa.s1pdgs.cpoc.report;

import java.util.Collections;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public final class ReportingUtils {
	public static final Reporting.Builder newReportingBuilder() {
		return new ReportAdapter.Builder(new LoggerReportingAppender());
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ProductFamily family, final String filename) {
		if (family == ProductFamily.L0_SEGMENT) {
			return new FilenameReportingInput(
					Collections.emptyList(),
					Collections.singletonList(filename)
			);
		}
		return new FilenameReportingInput(
				Collections.singletonList(filename),
				Collections.emptyList()
		);
	}
	
	public static final ReportingOutput newFilenameReportingOutputFor(final ProductFamily family, final String filename) {
		if (family == ProductFamily.L0_SEGMENT) {
			return new FilenameReportingOutput(
					Collections.emptyList(),
					Collections.singletonList(filename)
			);
		}
		return new FilenameReportingOutput(
				Collections.singletonList(filename),
				Collections.emptyList()
		);
	}
}
