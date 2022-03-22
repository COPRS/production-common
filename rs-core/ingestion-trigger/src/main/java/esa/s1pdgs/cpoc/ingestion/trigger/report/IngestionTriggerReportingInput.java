package esa.s1pdgs.cpoc.ingestion.trigger.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class IngestionTriggerReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {
	
	@JsonProperty("pickup_point_detection_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date pollingDate = new Date();
	
	@JsonProperty("pickup_point_available_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date availDate = new Date();

	public IngestionTriggerReportingInput(final ReportingFilenameEntries entry, final Date pollingDate, final Date availDate) {
		super(entry);
		this.pollingDate = pollingDate;
		this.availDate = availDate;
	}
	
	@JsonIgnore
	public static final IngestionTriggerReportingInput newInstance(
			final String productName,
			final ProductFamily family,
			final Date availDate
	) {
		return new IngestionTriggerReportingInput(
				new ReportingFilenameEntries(new ReportingFilenameEntry(family, productName)), 
				new Date(), 
				availDate
		);
	}

	public Date getPollingDate() {
		return pollingDate;
	}

	public void setPollingDate(final Date pollingDate) {
		this.pollingDate = pollingDate;
	}

	public Date getAvailDate() {
		return availDate;
	}

	public void setAvailDate(final Date availDate) {
		this.availDate = availDate;
	}
}
