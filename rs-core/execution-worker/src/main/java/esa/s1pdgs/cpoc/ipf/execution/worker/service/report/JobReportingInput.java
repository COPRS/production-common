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

package esa.s1pdgs.cpoc.ipf.execution.worker.service.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public class JobReportingInput extends AbstractFilenameReportingProduct implements ReportingInput {	
	@JsonProperty("job_order_id_string")
	private String jobOrderUuid;
	
	@JsonProperty("ipf_release_string")
	private String ipfVersion;
	
	public JobReportingInput(final ReportingFilenameEntries entries, final String jobOrderUuid,
			final String ipfVersion) {
		super(entries);
		this.jobOrderUuid = jobOrderUuid;
		this.ipfVersion = ipfVersion;
	}
	
	@JsonIgnore
	public static final JobReportingInput newInstance(
			final List<ReportingFilenameEntry> entries,
			final String jobOrderUuid,
			final String ipfVersion
	) {		
		return new JobReportingInput(new ReportingFilenameEntries(entries), jobOrderUuid, ipfVersion);
	}

	public String getJobOrderUuid() {
		return jobOrderUuid;
	}

	public void setJobOrderUuid(final String jobOrderUuid) {
		this.jobOrderUuid = jobOrderUuid;
	}

	public String getIpfVersion() {
		return ipfVersion;
	}

	public void setIpfVersion(String ipfVersion) {
		this.ipfVersion = ipfVersion;
	}

}
