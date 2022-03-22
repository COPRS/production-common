package esa.s1pdgs.cpoc.report;

public interface ReportAppender {
	static final ReportAppender NULL = new ReportAppender() {			
		@Override
		public void report(final ReportEntry entry) {
			// do nothing for null object 				
		}
	};		
	void report(ReportEntry entry);
}