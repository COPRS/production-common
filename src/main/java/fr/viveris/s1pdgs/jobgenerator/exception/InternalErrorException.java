/**
 * 
 */
package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class InternalErrorException extends AbstractCodedException {

	private static final long serialVersionUID = -7488001919910076897L;
	
	/**
	 * 
	 * @param message
	 */
	public InternalErrorException(final String message) {
		super(ErrorCode.INTERNAL_ERROR, message);
	}
	
	/**
	 * 
	 * @param message
	 * @param e
	 */
	public InternalErrorException(final String message, final Throwable e) {
		super(ErrorCode.INTERNAL_ERROR, message, e);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
