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

	void begin(String comment, final Object... objects);

	void intermediate(String comment, Object... objects);

	void end(String comment, final Object... objects);
	
	void endWithTransfer(String comment, long transferAmount, final Object... objects);

	void error(String comment, Object... objects);

}