package esa.s1pdgs.cpoc.prip.worker.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.input.SegmentReportingInput;

public class PripSegmentReportingInput extends SegmentReportingInput {	
	@JsonProperty("prip_storage_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date storeDate;

	public PripSegmentReportingInput(final String filename, final Date storeDate) {
		super(filename);
		this.storeDate = storeDate;
	}

	public Date getStoreDate() {
		return storeDate;
	}

	public void setStoreDate(final Date storeDate) {
		this.storeDate = storeDate;
	}
}
