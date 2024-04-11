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

package de.werum.coprs.requestparkinglot.rest;

import java.util.ArrayList;
import java.util.List;

/**
 * Message container in case of successfull operations
 */
public class ApiResponse {
	private String entity;
	private String action;
	private List<String> idsWithSuccess = new ArrayList<>();
	private List<String> idsSkipped = new ArrayList<>();
	
	public ApiResponse() {
	}
	
	public ApiResponse(String entity, String action, List<String> idsWithSuccess, List<String> idsSkipped) {
		this.entity = entity;
		this.action = action;
		this.idsWithSuccess = idsWithSuccess;
		this.idsSkipped = idsSkipped;
	}
	
	public String getEntity() {
		return entity;
	}
	
	public void setEntity(String entity) {
		this.entity = entity;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	public List<String> getIdsWithSuccess() {
		return idsWithSuccess;
	}
	
	public void setIdsWithSuccess(List<String> idsWithSuccess) {
		this.idsWithSuccess = idsWithSuccess;
	}
	
	public List<String> getIdsSkipped() {
		return idsSkipped;
	}
	
	public void setIdsSkipped(List<String> idsSkipped) {
		this.idsSkipped = idsSkipped;
	}

	@Override
	public String toString() {
		return "ApiResponse [entity=" + entity + ", action=" + action + ", idsWithSuccess=" + idsWithSuccess
				+ ", idsSkipped=" + idsSkipped + "]";
	}
}
