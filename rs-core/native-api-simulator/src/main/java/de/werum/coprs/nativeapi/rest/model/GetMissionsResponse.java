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

package de.werum.coprs.nativeapi.rest.model;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "Missions")
public class GetMissionsResponse {

	@Schema(example = "[\"s1\", \"s2\", \"s3\"]", description = "the names of the missions supported by the API")
	private List<String> missions = new ArrayList<>();

	public GetMissionsResponse(final List<String> missions) {
		this.missions = missions;
	}

	public List<String> getMissions() {
		return this.missions;
	}

	public void setMissions(final List<String> missions) {
		this.missions = missions;
	}
}
