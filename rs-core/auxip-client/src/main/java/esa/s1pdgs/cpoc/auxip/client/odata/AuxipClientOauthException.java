package esa.s1pdgs.cpoc.auxip.client.odata;

@SuppressWarnings("serial")
public class AuxipClientOauthException extends RuntimeException {
	public AuxipClientOauthException(final String message) {
		super(message);
	}
	public AuxipClientOauthException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
