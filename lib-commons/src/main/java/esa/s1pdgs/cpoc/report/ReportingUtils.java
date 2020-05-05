package esa.s1pdgs.cpoc.report;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public final class ReportingUtils {
	
	//  segment-blacklist-pattern ^S1([A-Z_]{1}).*(GP|HK|RF).*SAFE$: 
	private static String segmentBlacklistPattern = "^S1([A-Z_]{1}).*(GP|HK|RF).*SAFE$";

	public static void setSegmentBlacklistPattern(final String segmentBlacklistPattern) {
		ReportingUtils.segmentBlacklistPattern = segmentBlacklistPattern;
	}
	
	public static final Reporting.Builder newReportingBuilder() {
		return new ReportAdapter.Builder(new LoggerReportingAppender());
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ProductFamily family, final String filename) {
		if (family == ProductFamily.L0_SEGMENT && !filename.matches(segmentBlacklistPattern)) {
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
		if (family == ProductFamily.L0_SEGMENT && !filename.matches(segmentBlacklistPattern)) {
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
	
	// S1PRO-1395: makes sure that only the actual filename is dumped
	public static final List<String> toFlatFilenames(final List<String> filenames) {
		return filenames.stream()
				.map(p -> toFlatFilename(p))
				.collect(Collectors.toList());
	}
	
	public static final String toFlatFilename(final String filename) {
		return new File(filename).getName(); 
	}
}
