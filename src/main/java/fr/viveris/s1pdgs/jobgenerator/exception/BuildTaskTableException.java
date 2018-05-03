package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception during task table building
 * @author Cyrielle Gailliard
 *
 */
public class BuildTaskTableException extends JobGenerationException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -4598149920303190179L;

	/**
	 * Constructor
	 * @param message
	 * @param cause
	 * @param taskTable
	 */
	public BuildTaskTableException(String taskTable, String message, Throwable cause) {
		super(taskTable, ErrorCode.JOB_GENERATOR_INIT_FAILED, message, cause);
	}

}
