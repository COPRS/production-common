/**
 * 
 */
package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class KafkaException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7488001919910076897L;

	/**
	 * Constructor
	 * @param message
	 */
	public KafkaException(String message) {
		super(message);
	}

	/**
	 * Constructor
	 * @param message
	 */
	public KafkaException(String message, Throwable e) {
		super(message, e);
	}
}
