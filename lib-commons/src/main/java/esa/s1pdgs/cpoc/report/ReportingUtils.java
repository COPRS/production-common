package esa.s1pdgs.cpoc.report;

public final class ReportingUtils {
	public static final Reporting.Builder newReportingBuilder() {
		return new ReportAdapter.Builder(new LoggerReportingAppender());
	}
}
