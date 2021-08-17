package de.werum.csgrs.nativeapi.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PingResponse")
public class PingResponse {

	@Schema(example = "1.0", description = "the version of the API")
	private String apiVersion;

	public PingResponse(final String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getApiVersion() {
		return this.apiVersion;
	}

	public void setApiVersion(final String apiVersion) {
		this.apiVersion = apiVersion;
	}
}
