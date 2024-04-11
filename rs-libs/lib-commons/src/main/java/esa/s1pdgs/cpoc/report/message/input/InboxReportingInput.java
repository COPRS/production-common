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

package esa.s1pdgs.cpoc.report.message.input;

import com.fasterxml.jackson.annotation.JsonProperty;

import esa.s1pdgs.cpoc.report.ReportingInput;

public class InboxReportingInput implements ReportingInput {	
	
	@JsonProperty("inbox_name_string")
	private String name;
	
	@JsonProperty("inbox_relative_path_string")
	private String relativePath;
	
	@JsonProperty("inbox_pickup_path_string")
	private String pickupPath;

	public InboxReportingInput(String name, String relativePath, String pickupPath) {
		this.name = name;
		this.relativePath = relativePath;
		this.pickupPath = pickupPath;
	}

	public InboxReportingInput() {
		this(null, null, null);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRelativePath() {
		return relativePath;
	}

	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}

	public String getPickupPath() {
		return pickupPath;
	}

	public void setPickupPath(String pickupPath) {
		this.pickupPath = pickupPath;
	}
}
