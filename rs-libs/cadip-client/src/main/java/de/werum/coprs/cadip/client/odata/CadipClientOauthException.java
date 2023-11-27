package de.werum.coprs.cadip.client.odata;

@SuppressWarnings("serial")
public class CadipClientOauthException extends RuntimeException {
	public CadipClientOauthException(final String message) {
		super(message);
	}
	public CadipClientOauthException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
