package de.werum.coprs.nativeapi.rest.model.stac;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StacError implements Serializable {
	
	private static final long serialVersionUID = -3876313933452263651L;

	@JsonProperty("ErrorMessage")
	private String errorMessage;
	
	@JsonProperty("ErrorCode")
	private int errorCode;

	public StacError(String errorMessage, int errorCode) {
		super();
		this.errorMessage = errorMessage;
		this.errorCode = errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
	
}
