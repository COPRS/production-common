package esa.s1pdgs.cpoc.report;

public final class ReportingUtils {
	public static final Reporting.Builder newReportingBuilderFor(final String task) {
		return new ReportAdapter.Builder(new LoggerReportingAppender(), task);
	}
}
