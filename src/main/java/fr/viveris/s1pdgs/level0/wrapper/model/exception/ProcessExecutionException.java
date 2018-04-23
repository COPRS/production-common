/**
 * 
 */
package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class ProcessExecutionException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7488001919910076897L;

	/**
	 * Constructor
	 * @param message
	 */
	public ProcessExecutionException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * @param message
	 */
	public ProcessExecutionException(String message, Throwable e) {
		super(message, e);
	}
}
