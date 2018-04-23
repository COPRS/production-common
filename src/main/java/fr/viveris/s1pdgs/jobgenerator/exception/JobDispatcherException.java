package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception during job dispatch
 * @author Cyrielle Gailliard
 *
 */
public class JobDispatcherException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3113715609060082594L;

	/**
	 * Constructor
	 * @param message
	 */
	public JobDispatcherException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * @param message
	 * @param cause
	 */
	public JobDispatcherException(String message, Throwable cause) {
		super(message, cause);
	}

}
