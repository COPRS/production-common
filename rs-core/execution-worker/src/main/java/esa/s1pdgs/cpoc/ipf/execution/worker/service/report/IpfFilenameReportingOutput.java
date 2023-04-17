package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IpfFilenameReportingOutput extends FilenameReportingOutput {
	private final boolean debug;
	
	@JsonProperty("t0_pdgs_date")
	private final String lastInputAvailableDate;
	
	public IpfFilenameReportingOutput(final ReportingFilenameEntries entries, final boolean debug, final String lastInputAvailableDate) {
		super(entries);
		this.debug = debug;
		this.lastInputAvailableDate = lastInputAvailableDate;
	}
	
	public boolean getDebug() {
		return debug;
	}
}
