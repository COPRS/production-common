/**
 * 
 */
package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception occurred during job generation
 * @author Viveris Technologies
 *
 */
public class ProcessTimeoutException extends AbstractCodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7488001919910076897L;

	/**
	 * Constructor
	 * @param message
	 */
	public ProcessTimeoutException(final String message) {
		super(ErrorCode.PROCESS_TIMEOUT, message);
	}

	/**
	 * @see AbstractCodedException#getLogMessage()
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
