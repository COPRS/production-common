/**
 * 
 */
package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class JobGenerationException extends JobException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7488001919910076897L;
	
	/**
	 * Generic message
	 */
	private static final String MESSAGE = "[session %s] [taskTable %s] Job generation failed: %s";

	/**
	 * Constructor
	 * @param message
	 * @param taskTable
	 */
	public JobGenerationException(String message, String taskTable, String session) {
		super(String.format(MESSAGE, session, taskTable, message), taskTable);
	}

	/**
	 * Constructor
	 * @param message
	 * @param cause
	 * @param taskTable
	 */
	public JobGenerationException(String message, Throwable cause, String taskTable, String session) {
		super(String.format(MESSAGE, session, taskTable, message), cause, taskTable);
	}

}
