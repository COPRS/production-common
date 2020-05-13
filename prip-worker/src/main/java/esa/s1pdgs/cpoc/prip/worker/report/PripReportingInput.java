package esa.s1pdgs.cpoc.prip.worker.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class PripReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {	
	@JsonProperty("prip_storage_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date storeDate;

	public PripReportingInput(final ReportingFilenameEntries entries, final Date storeDate) {
		super(entries);
		this.storeDate = storeDate;
	}

	@JsonIgnore
	public static final PripReportingInput newInstance(
			final String productName, 
			final Date storeDate,
			final ProductFamily family
	) {
		return new PripReportingInput(
				new ReportingFilenameEntries(
						new ReportingFilenameEntry(family, productName)), 
				storeDate
		);
	}

	public Date getStoreDate() {
		return storeDate;
	}
}
