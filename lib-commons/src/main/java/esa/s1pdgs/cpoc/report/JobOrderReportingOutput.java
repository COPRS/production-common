package esa.s1pdgs.cpoc.report;

import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JobOrderReportingOutput implements ReportingOutput {	
	@JsonProperty("job_order_id_string")
	private String jobOrderUuid;
	
	@JsonProperty("job_order_parameters_object")
	private Map<String,String> jobOrderParameters;
	
	public JobOrderReportingOutput(String jobOrderUuid, Map<String, String> jobOrderParameters) {
		this.jobOrderUuid = jobOrderUuid;
		this.jobOrderParameters = jobOrderParameters;
	}
	
	public JobOrderReportingOutput() {
		this("0000-0000", Collections.emptyMap());
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
