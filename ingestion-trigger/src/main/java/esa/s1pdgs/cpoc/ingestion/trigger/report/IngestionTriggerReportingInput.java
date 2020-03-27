package esa.s1pdgs.cpoc.ingestion.trigger.report;

import java.util.Collections;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.input.FilenameReportingInput;

public class IngestionTriggerReportingInput extends FilenameReportingInput {
	
	@JsonProperty("pickup_point_detection_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date pollingDate = new Date();
	
	@JsonProperty("pickup_point_available_date")
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date availDate = new Date();
	

	public IngestionTriggerReportingInput(final String filename, final Date pollingDate, final Date availDate) {
		super(Collections.singletonList(filename), Collections.emptyList());
		this.pollingDate = pollingDate;
		this.availDate = availDate;
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
