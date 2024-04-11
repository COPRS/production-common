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

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class GhostHandlingSegmentReportingOutput implements ReportingOutput {
	@JsonProperty("is_ghost_candidate_boolean")
	private boolean isGhostCandidate;

	public GhostHandlingSegmentReportingOutput(final boolean isGhostCandidate) {
		this.isGhostCandidate = isGhostCandidate;
	}

	public boolean getIsGhostCandidate() {
		return isGhostCandidate;
	}

	public void setIsGhostCandidate(final boolean isGhostCandidate) {
		this.isGhostCandidate = isGhostCandidate;
	}	
}
