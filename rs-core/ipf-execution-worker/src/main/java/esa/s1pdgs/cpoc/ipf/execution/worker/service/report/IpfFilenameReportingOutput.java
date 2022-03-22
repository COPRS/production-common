package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IpfFilenameReportingOutput extends FilenameReportingOutput {
	private final boolean debug;
	
	public IpfFilenameReportingOutput(final ReportingFilenameEntries entries, final boolean debug) {
		super(entries);
		this.debug = debug;
	}
	
	public boolean getDebug() {
		return debug;
	}
}
