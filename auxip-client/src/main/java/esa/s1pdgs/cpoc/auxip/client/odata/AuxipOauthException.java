package esa.s1pdgs.cpoc.auxip.client.odata;

@SuppressWarnings("serial")
public class AuxipOauthException extends RuntimeException {
	public AuxipOauthException(final String message) {
		super(message);
	}
	public AuxipOauthException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
