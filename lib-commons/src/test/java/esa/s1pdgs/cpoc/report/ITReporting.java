package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.message.output.JobOrderReportingOutput;

public final class ITReporting {
	private static final Logger LOG = LogManager.getLogger(ITReporting.class);
	
	@Test
	public final void testReportingVsLogging() {
		Reporting uut = ReportingUtils.newReportingBuilder().newReporting("test");
		
		uut.begin(new ReportingMessage("Foo"));		
		uut.end(new ReportingMessage(42000L, "baz"));	
		
		uut = uut.newReporting("test.child");
		uut.begin(new ReportingMessage("Foo"));		
		uut.end(new JobOrderReportingOutput(UUID.randomUUID().toString(), Collections.singletonMap("foo_string", "bar")), new ReportingMessage(42000L, "baz"));
		
		uut = ReportingUtils.newReportingBuilder().newReporting("test2");
		uut.begin(new ReportingMessage("Foo"));

		
		
		uut.end(ReportingUtils.newFilenameReportingOutputFor(ProductFamily.BLANK, "fooBar.txt"), new ReportingMessage(230000000L,"Foo"));
		
		//LOG.debug("foo bar");
	}
}
