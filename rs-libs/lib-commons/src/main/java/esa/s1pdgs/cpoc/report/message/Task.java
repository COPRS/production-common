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

import esa.s1pdgs.cpoc.report.Reporting.Event;

@JsonInclude(Include.NON_NULL)
public class Task {
	private String uid;
	private String name;
	private Event event;	
	private String satellite;
	
	public Task() {
	}
	
	public Task(final String uid, final String name, final Event event) {
		this.uid = uid;
		this.name = name;
		this.event = event;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(final String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public Event getEvent() {
		return event;
	}

	public void setEvent(final Event event) {
		this.event = event;
	}

	public String getSatellite() {
		return satellite;
	}

	public void setSatellite(final String satellite) {
		this.satellite = satellite;
	}
}
