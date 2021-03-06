package esa.s1pdgs.cpoc.report;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.metadata.model.MissionId;
import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public final class ReportingUtils {
	// default pattern to use if no other is configured
	private static String segmentBlacklistPattern = "^S1([A-Z_]{1}).*(GP|HK).*SAFE(.zip)?$";
	
	private static final Predicate<ReportingFilenameEntry> SEGMENT_FILTER = e -> {
		return ( e.getFamily().equals(ProductFamily.L0_SEGMENT) && !toFlatFilename(e.getProductName())
				.matches(segmentBlacklistPattern));
	};

	// This is dirty but the easiest way without modifying 
	public static void setSegmentBlacklistPattern(final String segmentBlacklistPattern) {
		ReportingUtils.segmentBlacklistPattern = segmentBlacklistPattern;
	}
	
	public static final Reporting.Builder newReportingBuilder(MissionId mission) {
		return new ReportAdapter.Builder(new LoggerReportingAppender(), mission);
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ProductFamily family, final String name) {
		return newFilenameReportingInputFor(new ReportingFilenameEntry(family, name));
	}
	
	public static final ReportingInput newFilenameReportingInputFor(final ReportingFilenameEntry ... products) {
		return new FilenameReportingInput(new ReportingFilenameEntries(Arrays.asList(products)));
	}
	
	public static final ReportingOutput newFilenameReportingOutputFor(final ProductFamily family, final String name) {
		return newFilenameReportingOutputFor(new ReportingFilenameEntry(family, name));
	}
	
	public static final ReportingOutput newFilenameReportingOutputFor(final ReportingFilenameEntry ... products) {
		return new FilenameReportingOutput(new ReportingFilenameEntries(Arrays.asList(products)));
	}
	
	static List<String> segmentsOf(final List<ReportingFilenameEntry> products) {
		return uniqueFlatProducts(products, SEGMENT_FILTER);				
	}
	
	static List<String> filenamesOf(final List<ReportingFilenameEntry> products) {
		// everything not matching the segment filter will be reported as 'filename'
		return uniqueFlatProducts(products, not(SEGMENT_FILTER));				
	}
		
	private static final <E> Predicate<E> not(final Predicate<E> predicate) {
		return predicate.negate();
	}
	
	private static final List<String> uniqueFlatProducts(
			final List<ReportingFilenameEntry> products,
			final Predicate<ReportingFilenameEntry> filter
	) {
		return products.stream()
				.filter(filter)
				.map(e -> toFlatFilename(e.getProductName()))
				.collect(Collectors.toList());
	}
	

	// S1PRO-1395: makes sure that only the actual filename is dumped	
	private static final String toFlatFilename(final String filename) {
		return new File(filename).getName(); 
	}
}
