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
