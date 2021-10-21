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
