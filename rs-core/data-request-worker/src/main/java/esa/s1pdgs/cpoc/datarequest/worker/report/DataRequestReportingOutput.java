/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
