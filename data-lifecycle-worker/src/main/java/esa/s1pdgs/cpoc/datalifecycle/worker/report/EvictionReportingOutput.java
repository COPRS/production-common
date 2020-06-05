package esa.s1pdgs.cpoc.datalifecycle.worker.report;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class EvictionReportingOutput implements ReportingOutput {
	@JsonProperty("eviction_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date evictionDate;

	public EvictionReportingOutput(final Date evictionDate) {
		this.evictionDate = evictionDate;
	}

	public Date getEvictionDate() {
		return evictionDate;
	}
}
