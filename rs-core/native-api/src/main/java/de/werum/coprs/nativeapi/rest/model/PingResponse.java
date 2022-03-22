package de.werum.coprs.nativeapi.rest.model;

public class PingResponse {

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
