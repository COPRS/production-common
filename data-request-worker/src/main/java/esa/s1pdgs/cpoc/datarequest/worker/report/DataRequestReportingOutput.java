package esa.s1pdgs.cpoc.datarequest.worker.report;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class DataRequestReportingOutput implements ReportingOutput {
	
	@JsonProperty("data_request_type_string")
	private String dataRequestType;
	
	public DataRequestReportingOutput(final String dataRequstType) {
		this.dataRequestType = dataRequstType;
	}

	public String getDataRequestType() {
		return dataRequestType;
	}

	public void setDataRequestType(final String dataRequestType) {
		this.dataRequestType = dataRequestType;
	}

}
