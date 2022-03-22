package esa.s1pdgs.cpoc.report.message;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.Reporting.Event;
import esa.s1pdgs.cpoc.report.ReportingInput;

@JsonInclude(Include.NON_NULL)
public class BeginTask extends Task {
	private ReportingInput input;
	
	@JsonProperty("child_of_task")
	private String childOfTask;
	@JsonProperty("follows_from_task")
	private String followsFromTask;
	
	public BeginTask() {
		super();
	}

	public BeginTask(final String uid, final String name, final ReportingInput input) {
		super(uid, name, Event.BEGIN);
		this.input = input;
	}

	public ReportingInput getInput() {
		return input;
	}
	
	public void setInput(final ReportingInput input) {
		this.input = input;
	}
	
	public String getChildOfTask() {
		return childOfTask;
	}
	
	public void setChildOfTask(final String childOfTask) {
		this.childOfTask = childOfTask;
	}
	
	public String getFollowsFromTask() {
		return followsFromTask;
	}
	
	public void setFollowsFromTask(final String followsFromTask) {
		this.followsFromTask = followsFromTask;
	}
}
