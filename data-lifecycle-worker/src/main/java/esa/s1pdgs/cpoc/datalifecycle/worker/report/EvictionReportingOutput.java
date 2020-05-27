package esa.s1pdgs.cpoc.datalifecycle.worker.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingOutput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public class EvictionReportingOutput extends AbstractFilenameReportingProduct implements ReportingOutput {
	@JsonProperty("eviction_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date evictionDate;
	
	@JsonProperty("last_modified_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date lastModified;

	public EvictionReportingOutput(
			final ReportingFilenameEntries entries, 
			final Date evictionDate, 
			final Date lastModified
	) {
		super(entries);
		this.evictionDate = evictionDate;
		this.lastModified = lastModified;
	}
	
	@JsonIgnore
	public static final EvictionReportingOutput newInstance(
			final String productName, 
			final Date evictionDate,
			final Date lastModified,
			final ProductFamily family
	) {
		return new EvictionReportingOutput(
				new ReportingFilenameEntries(new ReportingFilenameEntry(family, productName)), 
				evictionDate,
				lastModified
		);
	}

	public Date getEvictionDate() {
		return evictionDate;
	}

	public Date getLastModified() {
		return lastModified;
	}	
}
