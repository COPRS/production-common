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

package esa.s1pdgs.cpoc.dlq.manager.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.dlq.manager.model.routing.ActionType;
import esa.s1pdgs.cpoc.report.ReportingOutput;

public final class DlqReportingOutput implements ReportingOutput {	

	@JsonProperty("dlq_error_title")
	private String errorTitle;
	
	@JsonProperty("dlq_action_type")
	private ActionType actionType;
	
	@JsonProperty("dlq_route_to")
	private String routeTo;
	
	public DlqReportingOutput(final String errorTitle, final ActionType actionType, final String routeTo) {
		this.errorTitle = errorTitle;
		this.actionType = actionType;
		this.routeTo = routeTo;
	}

	@JsonIgnore
	public static final DlqReportingOutput newInstance(
			final String errorTitle, final ActionType actionType, final String routeTo
	) {
		return new DlqReportingOutput(errorTitle, actionType, routeTo);
	}

	public String getErrorTitle() {
		return errorTitle;
	}

	public ActionType getActionType() {
		return actionType;
	}

	public String getRouteTo() {
		return routeTo;
	}
}
