package esa.s1pdgs.cpoc.report;

public interface ReportingFactory {	
	static final ReportingFactory NULL = new ReportingFactory() {
		@Override public final Reporting newReporting(final String taskName) {
			return Reporting.NULL;
		}		
	};
	
	Reporting newReporting(String taskName);
}
