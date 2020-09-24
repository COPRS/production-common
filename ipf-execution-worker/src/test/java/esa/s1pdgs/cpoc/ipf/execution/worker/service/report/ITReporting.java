package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import org.junit.Test;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.Reporting;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingMessage;
import esa.s1pdgs.cpoc.report.ReportingUtils;

public final class ITReporting {
	
	@Test
	public final void testReportingVsLogging() {
		final Reporting uut = ReportingUtils.newReportingBuilder().newReporting("test");
		
		uut.begin(new ReportingMessage("test message"));	
		uut.end(new IpfFilenameReportingOutput(
				new ReportingFilenameEntries(new ReportingFilenameEntry(ProductFamily.L0_SEGMENT, "S1A_RF_RAW__0SHV_20200120T123137_20200120T123138_030884_038B5A_FCFB.SAFE")), true), 
				new ReportingMessage(230000000L,"test message")
		);

	}
}
