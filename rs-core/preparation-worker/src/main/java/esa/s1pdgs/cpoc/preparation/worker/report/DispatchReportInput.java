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

package esa.s1pdgs.cpoc.preparation.worker.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.ReportingFilenameEntry;
import esa.s1pdgs.cpoc.report.ReportingInput;
import esa.s1pdgs.cpoc.report.message.AbstractFilenameReportingProduct;

public final class DispatchReportInput extends AbstractFilenameReportingProduct implements ReportingInput  {	
	@JsonProperty("job_id_long")
	private long jobId;
	
	@JsonProperty("input_type_string")
	private String inputType;
	
	public DispatchReportInput(final ReportingFilenameEntries entries, final long jobId, final String inputType) {
		super(entries);
		this.jobId = jobId;
		this.inputType = inputType;
	}
	
	@JsonIgnore
	public static final DispatchReportInput newInstance(final long jobId, final String filename, final String inputType) {        
		final ProductFamily family = inputType.equals("L0Segment") ?
				ProductFamily.L0_SEGMENT :
			    ProductFamily.BLANK; // we only care about segments here, everything else will be reported as 'filename'
		
		return new DispatchReportInput(
				new ReportingFilenameEntries(
						new ReportingFilenameEntry(family, filename)), 
				jobId, 
    			inputType
		);
	}
	
	public String getInputType() {
		return inputType;
	}

	public long getJobId() {
		return jobId;
	}
}
