package esa.s1pdgs.cpoc.prip.worker.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public final class PripReportingOutput implements ReportingOutput {	
	@JsonProperty("prip_storage_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date storeDate;

	public PripReportingOutput(final Date storeDate) {
		this.storeDate = storeDate;
	}

	@JsonIgnore
	public static final PripReportingOutput newInstance(
			final Date storeDate
	) {
		return new PripReportingOutput(storeDate);
	}

	public Date getStoreDate() {
		return storeDate;
	}
}
