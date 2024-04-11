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

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import esa.s1pdgs.cpoc.metadata.model.MissionId;

import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
public class Header {
	private String type = "REPORT";	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'000Z'", timezone="UTC")
	private Date timestamp = new Date();
	private Level level;
	private MissionId mission;
	private String workflow = "NOMINAL";
	@JsonProperty("debug_mode")
	private Boolean debug;
	@JsonProperty("tag_list")
	private List<String> tags;
	@JsonProperty("rs_chain_name")
	private String rsChainName;
	@JsonProperty("rs_chain_version")
	private String rsChainVersion;
	
	public Header() {

	}
	
	public Header(final Level level, final MissionId mission) {
		this.level = level;
		this.mission = mission;
	}

	public String getType() {
		return type;
	}
		
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(final Date timestamp) {
		this.timestamp = timestamp;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public void setLevel(final Level level) {
		this.level = level;
	}
	
	public MissionId getMission() {
		return mission;
	}

	public void setMission(MissionId mission) {
		this.mission = mission;
	}

	public String getWorkflow() {
		return workflow;
	}
	
	public void setWorkflow(final String workflow) {
		this.workflow = workflow;
	}

	public Boolean getDebug() {
		return debug;
	}

	public void setDebug(final Boolean debug) {
		this.debug = debug;
	}

	public List<String> getTags() {
		return tags;
	}
	
	public void setTags(final List<String> tags) {
		this.tags = tags;
	}

	public String getRsChainName() {
		return rsChainName;
	}

	public void setRsChainName(final String rsChainName) {
		this.rsChainName = rsChainName;
	}

	public String getRsChainVersion() {
		return rsChainVersion;
	}

	public void setRsChainVersion(final String rsChainVersion) {
		this.rsChainVersion = rsChainVersion;
	}
	
}
