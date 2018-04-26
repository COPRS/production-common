/**
 * 
 */
package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class ProcessTimeoutException extends CodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7488001919910076897L;

	/**
	 * Constructor
	 * @param message
	 */
	public ProcessTimeoutException(String message) {
		super(ErrorCode.PROCESS_TIMEOUT, message);
	}

	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
