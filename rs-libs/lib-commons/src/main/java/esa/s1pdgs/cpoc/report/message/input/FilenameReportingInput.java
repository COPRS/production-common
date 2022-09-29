package esa.s1pdgs.cpoc.report.message.input;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class FilenameReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {	
	public FilenameReportingInput(final ReportingFilenameEntries entries) {
		super(entries);
	}	
}
