/**
 * 
 */
package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception occurred during job generation
 * 
 * @author Cyrielle Gailliard
 *
 */
public class MaxNumberCachedSessionsReachException extends AbstractCodedException {

	private static final long serialVersionUID = -7488001919910076897L;

	/**
	 * 
	 * @param message
	 */
	public MaxNumberCachedSessionsReachException(final String message) {
		super(ErrorCode.MAX_NUMBER_CACHED_SESSIONS_REACH, message);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
