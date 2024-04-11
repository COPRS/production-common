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

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingFilenameEntries;
import esa.s1pdgs.cpoc.report.message.output.FilenameReportingOutput;

public class IpfFilenameReportingOutput extends FilenameReportingOutput {
	private final boolean debug;
	
	@JsonProperty("t0_pdgs_date")
	private final String lastInputAvailableDate;
	
	public IpfFilenameReportingOutput(final ReportingFilenameEntries entries, final boolean debug, final String lastInputAvailableDate) {
		super(entries);
		this.debug = debug;
		this.lastInputAvailableDate = lastInputAvailableDate;
	}
	
	public boolean getDebug() {
		return debug;
	}
}
