package esa.s1pdgs.cpoc.prip.worker.report;

import com.fasterxml.jackson.annotation.JsonIgnore;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class PripReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {	

	public PripReportingInput(final ReportingFilenameEntries entries) {
		super(entries);
	}

	@JsonIgnore
	public static final PripReportingInput newInstance(
			final String productName, 
			final ProductFamily family
	) {
		return new PripReportingInput(
				new ReportingFilenameEntries(new ReportingFilenameEntry(family, productName))
		);
	}

}
