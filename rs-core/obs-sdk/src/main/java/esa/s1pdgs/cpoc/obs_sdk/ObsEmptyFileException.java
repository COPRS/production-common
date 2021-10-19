package esa.s1pdgs.cpoc.obs_sdk;

public class ObsEmptyFileException extends Exception {

	private static final long serialVersionUID = -1667027224099918612L;

	public ObsEmptyFileException(final String message) {
		super(message);
	}

	public ObsEmptyFileException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
