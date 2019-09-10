package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobOrderReportingInput extends FilenameReportingInput {	
	@JsonProperty("jobOrderId_uuid")
	private String jobOrderUuid;
	
	@JsonProperty("jobOrderParameters_object")
	private Map<String,String> jobOrderParameters;
	
	public JobOrderReportingInput(List<String> filenames, String jobOrderUuid, Map<String, String> jobOrderParameters) {
		super(filenames);
		this.jobOrderUuid = jobOrderUuid;
		this.jobOrderParameters = jobOrderParameters;
	}
	
	public JobOrderReportingInput() {
		this(Collections.emptyList(),String.valueOf(UUID.randomUUID()), Collections.emptyMap());
	}

	public String getJobOrderUuid() {
		return jobOrderUuid;
	}

	public void setJobOrderUuid(String jobOrderUuid) {
		this.jobOrderUuid = jobOrderUuid;
	}

	public Map<String, String> getJobOrderParameters() {
		return jobOrderParameters;
	}

	public void setJobOrderParameters(Map<String, String> jobOrderParameters) {
		this.jobOrderParameters = jobOrderParameters;
	}
}
