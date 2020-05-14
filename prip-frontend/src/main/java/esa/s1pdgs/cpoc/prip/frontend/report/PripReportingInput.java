package esa.s1pdgs.cpoc.prip.frontend.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class PripReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {	
	@JsonProperty("user_name_string")
	private String userName;
	
	public PripReportingInput(final ReportingFilenameEntries entries, final String userName) {
		super(entries);
		this.userName = userName;
	}
	
	@JsonIgnore
	public static final PripReportingInput newInstance(
			final String productName, 
			final String userName, 
			final ProductFamily family
	) {
		return new PripReportingInput(
				new ReportingFilenameEntries(
						new ReportingFilenameEntry(family, productName)), 
				userName
		);
	}

	public String getUserName() {
		return userName;
	}
}
