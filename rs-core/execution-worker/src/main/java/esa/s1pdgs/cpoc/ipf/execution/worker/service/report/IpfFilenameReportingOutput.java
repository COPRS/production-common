package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IpfFilenameReportingOutput extends FilenameReportingOutput {
	private final boolean debug;
	
	@JsonProperty("t0_pdgs_date")
	private final Date lastInputAvailableDate;
	
	public IpfFilenameReportingOutput(final ReportingFilenameEntries entries, final boolean debug, final Date lastInputAvailableDate) {
		super(entries);
		this.debug = debug;
		this.lastInputAvailableDate = lastInputAvailableDate;
	}
	
	public boolean getDebug() {
		return debug;
	}
}
