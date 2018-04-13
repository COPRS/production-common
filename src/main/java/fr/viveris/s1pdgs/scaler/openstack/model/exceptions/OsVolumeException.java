package fr.viveris.s1pdgs.scaler.openstack.model.exceptions;

public class OsVolumeException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1694474051225086865L;

	public OsVolumeException() {
	}

	public OsVolumeException(String message) {
		super(message);
	}

	public OsVolumeException(Throwable cause) {
		super(cause);
	}

	public OsVolumeException(String message, Throwable cause) {
		super(message, cause);
	}

	public OsVolumeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
