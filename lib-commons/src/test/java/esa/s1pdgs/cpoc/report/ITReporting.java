package esa.s1pdgs.cpoc.report;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public final class ITReporting {
	private static final Logger LOG = LogManager.getLogger(ITReporting.class);
	
	@Test
	public final void testReporrtingVsLogging() {
		
		final Reporting uut= new LoggerReporting.Factory("test").newReporting(0);
		
		uut.begin(new ReportingMessage("Foo"));		
		uut.intermediate(new ReportingMessage("bar"));
		uut.end(new ReportingMessage(42000L, "baz"));	
		
		LOG.debug("foo bar");
	}

}
