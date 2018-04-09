package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsServerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1694474051225086865L;

	public OsServerException() {
	}

	public OsServerException(String message) {
		super(message);
	}

	public OsServerException(Throwable cause) {
		super(cause);
	}

	public OsServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public OsServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
