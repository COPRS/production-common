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
