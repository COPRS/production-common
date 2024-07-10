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

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class SeaCoverageCheckReportingOutput implements ReportingOutput {
	@JsonProperty("over_sea_boolean")
	private boolean overSea;

	public SeaCoverageCheckReportingOutput(final boolean overSea) {
		this.overSea = overSea;
	}
	
	public boolean getOverSea() {
		return overSea;
	}

	public void setOverSea(final boolean overSea) {
		this.overSea = overSea;
	}
}
