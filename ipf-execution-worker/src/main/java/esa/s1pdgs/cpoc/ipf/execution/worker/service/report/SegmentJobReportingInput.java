package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.message.input.SegmentReportingInput;

public class SegmentJobReportingInput extends SegmentReportingInput {	
	@JsonProperty("job_order_id_string")
	private String jobOrderUuid;
	
	public SegmentJobReportingInput(
			final List<String> filenames, 
			final String jobOrderUuid
	) {
		super(filenames);
		this.jobOrderUuid = jobOrderUuid;
	}
	
	public SegmentJobReportingInput() {
		this(Collections.emptyList(), String.valueOf(UUID.randomUUID()));
	}

	public String getJobOrderUuid() {
		return jobOrderUuid;
	}

	public void setJobOrderUuid(final String jobOrderUuid) {
		this.jobOrderUuid = jobOrderUuid;
	}
}
