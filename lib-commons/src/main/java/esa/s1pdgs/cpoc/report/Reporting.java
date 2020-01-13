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
		Reporting newTaskReporting(String taskName);
		void newEventReporting(final ReportingMessage reportingMessage);
	}

	public interface ChildFactory {
		public static final ChildFactory NULL = new ChildFactory() {
			@Override
			public Reporting newChild(String taskName) { return Reporting.NULL; }
		};

		Reporting newChild(String taskName);
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
	
	public static final Reporting NULL = new Reporting() {
		
		/* Discarding Reporting Implementation */

		@Override
		public void begin(ReportingInput input, ReportingMessage reportingMessage) {}

		@Override
		public void end(ReportingOutput output, ReportingMessage reportingMessage) {}

		@Override
		public void error(ReportingMessage reportingMessage) {}

		@Override
		public ChildFactory getChildFactory() {
			return Reporting.ChildFactory.NULL;
		}		
	};
		
	default void begin(final ReportingMessage reportingMessage) {
		begin(ReportingInput.NULL, reportingMessage);
	}
	
	default void end(final ReportingMessage reportingMessage) {
		end(ReportingOutput.NULL, reportingMessage);
	}	
	
	void begin(ReportingInput input, ReportingMessage reportingMessage);
	void end(ReportingOutput output, ReportingMessage reportingMessage);
	void error(ReportingMessage reportingMessage);
	ChildFactory getChildFactory();
}