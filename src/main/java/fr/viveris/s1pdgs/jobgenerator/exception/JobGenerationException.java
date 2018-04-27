/**
 * 
 */
package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Abstract class for custom exception concerning jobs
 * 
 * @author Cyrielle Gailliard
 *
 */
public class JobGenerationException extends AbstractCodedException {

	private static final long serialVersionUID = -1059947384304413070L;

	/**
	 * Task table XML filename
	 */
	protected String taskTable;

	public JobGenerationException(String taskTable, ErrorCode code, String message) {
		super(code, message);
		this.taskTable = taskTable;
	}

	public JobGenerationException(String taskTable, ErrorCode code, String message, Throwable e) {
		super(code, message, e);
		this.taskTable = taskTable;
	}

	public String getTaskTable() {
		return taskTable;
	}

	@Override
	public String getLogMessage() {
		return String.format("[taskTable %s] [msg %s]", this.taskTable, this.getMessage());
	}
}
