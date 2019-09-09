package esa.s1pdgs.cpoc.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Reporting {
	public interface Factory {
		Reporting newReporting(int step);
	}
	
	enum Event {
		begin,
		intermediate,
		end
	}
	
	enum Status {
		OK,
		NOK
	}
	
	public static final Logger REPORT_LOG = LogManager.getLogger(Reporting.class); 

	void begin(ReportingMessage reportingMessage);
	
	void begin(ReportingInput input, ReportingMessage reportingMessage);

	void intermediate(ReportingMessage reportingMessage);

	void end(ReportingMessage reportingMessage);
	
	void end(ReportingOutput output, ReportingMessage reportingMessage);

	void error(ReportingMessage reportingMessage);

}