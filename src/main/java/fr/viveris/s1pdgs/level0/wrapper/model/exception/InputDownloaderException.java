package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception concerning the object storage
 * @author Cyrielle Gailliard
 *
 */
public class InputDownloaderException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Generic message
	 */
	private static final String MESSAGE = "[family %s] [filePath %s] %s";

	/**
	 * Constructor
	 * @param msg
	 * @param key
	 * @param bucket
	 * @param cause
	 */
	public InputDownloaderException(String msg) {
		super(msg);
	}

	/**
	 * Constructor
	 * @param msg
	 * @param key
	 * @param bucket
	 * @param cause
	 */
	public InputDownloaderException(String msg, String family, String filepath) {
		super(String.format(MESSAGE, family, filepath, msg));
	}

}
