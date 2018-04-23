/**
 * 
 */
package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Abstract class for custom exception concerning jobs
 * @author Cyrielle Gailliard
 *
 */
public class JobException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -1059947384304413070L;
	
	/**
	 * Task table XML filename
	 */
	protected String taskTable;
	
	/**
	 * Constructor
	 * @param message
	 */
	public JobException(String message, String taskTable) {
		super(message);
		this.taskTable = taskTable;
	}

	/**
	 * Constructor
	 * @param message
	 * @param cause
	 */
	public JobException(String message, Throwable cause, String taskTable) {
		super(message, cause);
		this.taskTable = taskTable;
	}

	/**
	 * @return the taskTable
	 */
	public String getTaskTable() {
		return taskTable;
	}

	/**
	 * @param taskTable the taskTable to set
	 */
	public void setTaskTable(String taskTable) {
		this.taskTable = taskTable;
	}
}
