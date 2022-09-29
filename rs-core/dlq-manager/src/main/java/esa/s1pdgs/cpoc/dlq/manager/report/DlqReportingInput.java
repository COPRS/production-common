package esa.s1pdgs.cpoc.dlq.manager.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public final class DlqReportingInput implements ReportingInput {	
	
	@JsonProperty("dlq_original_topic")
	private String originalTopic;
	
	@JsonProperty("dlq_retry_counter")
	private int retryCounter;

	public DlqReportingInput(final String originalTopic, final int retryCounter) {
		this.originalTopic = originalTopic;
		this.retryCounter = retryCounter;
	}

	@JsonIgnore
	public static final DlqReportingInput newInstance(
			final String originalTopic, final int retryCounter			
	) {
		return new DlqReportingInput(originalTopic, retryCounter);
	}

	public String getOriginalTopic() {
		return originalTopic;
	}

	public int getRetryCounter() {
		return retryCounter;
	}

}
