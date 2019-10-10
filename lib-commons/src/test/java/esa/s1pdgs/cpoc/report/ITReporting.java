package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

public final class ITReporting {
	private static final Logger LOG = LogManager.getLogger(ITReporting.class);
	
	@Test
	public final void testReportingVsLogging() {
		final Reporting.Factory fct = new LoggerReporting.Factory("test") ;
		
		Reporting uut= fct.newReporting(0);
		
		uut.begin(new ReportingMessage("Foo"));		
		uut.intermediate(new ReportingMessage("bar"));
		uut.end(new ReportingMessage(42000L, "baz"));	
		
		uut= fct.newReporting(1);
		uut.begin(new ReportingMessage("Foo"));		
		uut.intermediate(new ReportingMessage("bar"));
		uut.intermediate(new ReportingMessage("baaz"));
		uut.end(new JobOrderReportingOutput(UUID.randomUUID().toString(), Collections.singletonMap("foo_string", "bar")), new ReportingMessage(42000L, "baz"));
		
		uut = new LoggerReporting.Factory("test2").newReporting(0);
		uut.begin(new ReportingMessage("Foo"));
		uut.end(new FilenameReportingOutput(Collections.singletonList("fooBar.txt")), new ReportingMessage(230000000L,"Foo"));
		
		LOG.debug("foo bar");
	}

}
