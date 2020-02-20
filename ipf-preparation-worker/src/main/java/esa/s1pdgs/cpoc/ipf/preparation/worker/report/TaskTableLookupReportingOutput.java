package esa.s1pdgs.cpoc.ipf.preparation.worker.report;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingOutput;

public class TaskTableLookupReportingOutput implements ReportingOutput {

	@JsonProperty("tasktable_strings")
	private List<String> taskTableNames;
	
	public TaskTableLookupReportingOutput() {
		this(Collections.emptyList());
	}
	public TaskTableLookupReportingOutput(final List<String> taskTableNames) {
		this.taskTableNames = taskTableNames;
	}

	public List<String> getTaskTableNames() {
		return taskTableNames;
	}

	public void setTaskTableNames(final List<String> taskTableNames) {
		this.taskTableNames = taskTableNames;
	}
}
