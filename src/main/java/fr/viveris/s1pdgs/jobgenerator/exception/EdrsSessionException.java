package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception concerning an EDRS session file management
 * @author Cyrielle Gailliard
 *
 */
public class EdrsSessionException extends Exception {

	private static final long serialVersionUID = 3720211649870339168L;

	/**
	 * Constructor
	 * 
	 * @param message
	 */
	public EdrsSessionException(String message) {
		super(message);
	}
}
