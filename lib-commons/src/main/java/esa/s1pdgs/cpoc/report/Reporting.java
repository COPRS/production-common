package esa.s1pdgs.cpoc.report;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Reporting {
	public interface Builder {				
		Builder predecessor(UUID predecessor);
		default Builder addTag(final String tag) {
			return addTags(Collections.singleton(tag));
		}
		Builder addTags(Collection<String> tags);	
		Reporting newReporting();
	}
	
	enum Event {
		begin,
		end
	}
	
	enum Status {
		OK(0),
		NOK(1);
		
		private final int errCode;

		private Status(final int errCode) {
			this.errCode = errCode;
		}
		
		public final int errCode() {
			return errCode;
		}
	}
	
	public static final Logger REPORT_LOG = LogManager.getLogger(Reporting.class); 

	default void begin(final ReportingMessage reportingMessage) {
		begin(ReportingInput.NULL, reportingMessage);
	}
	
	default void end(final ReportingMessage reportingMessage) {
		end(ReportingOutput.NULL, reportingMessage);
	}	
	
	void begin(ReportingInput input, ReportingMessage reportingMessage);
	void end(ReportingOutput output, ReportingMessage reportingMessage);
	void error(ReportingMessage reportingMessage);	
	Reporting newChild(String childActionName);
}