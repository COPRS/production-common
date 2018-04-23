package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception during task table building
 * @author Cyrielle Gailliard
 *
 */
public class BuildTaskTableException extends JobException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4598149920303190179L;
	
	/**
	 * Generic message
	 */
	private static final String MESSAGE = "[tasktable %s] Initialization failed: %s";

	/**
	 * Constructor
	 * @param message
	 * @param cause
	 * @param taskTable
	 */
	public BuildTaskTableException(String message, Throwable cause, String taskTable) {
		super(String.format(MESSAGE, taskTable, message), cause, taskTable);
	}

}
