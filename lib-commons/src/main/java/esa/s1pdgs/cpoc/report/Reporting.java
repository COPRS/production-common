package esa.s1pdgs.cpoc.report;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface Reporting extends ReportingFactory {	
	public interface Builder extends ReportingFactory {		
		Builder predecessor(UUID predecessor);
		Builder root(UUID root);
		Builder parent(UUID parent);		
		default Builder addTag(final String tag) {
			return addTags(Collections.singleton(tag));
		}
		Builder addTags(Collection<String> tags);
		void newEventReporting(ReportingMessage reportingMessage);
	}
	
	enum Event {
		BEGIN,
		END
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
		@Override
		public UUID getUid() {
			return null;
		}
		@Override
		public UUID getRootUID() {
			return null;
		}

		@Override
		public void begin(final ReportingInput input, final ReportingMessage reportingMessage) {}

		@Override
		public void end(final ReportingOutput output, final ReportingMessage reportingMessage) {}

		@Override
		public void error(final ReportingMessage reportingMessage) {}

		@Override
		public Reporting newReporting(final String taskName) {
			return NULL;
		}
	};
	
	UUID getUid();

	UUID getRootUID();
	
	default void begin(final ReportingMessage reportingMessage) {
		begin(ReportingInput.NULL, reportingMessage);
	}
	
	default void end(final ReportingMessage reportingMessage) {
		end(ReportingOutput.NULL, reportingMessage);
	}	
	
	void begin(ReportingInput input, ReportingMessage reportingMessage);
	void end(ReportingOutput output, ReportingMessage reportingMessage);
	void error(ReportingMessage reportingMessage);
}