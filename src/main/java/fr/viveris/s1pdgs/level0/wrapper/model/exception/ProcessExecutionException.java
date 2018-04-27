/**
 * 
 */
package fr.viveris.s1pdgs.level0.wrapper.model.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class ProcessExecutionException extends CodedException {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -7488001919910076897L;
	
	private int exitCode;

	/**
	 * Constructor
	 * @param message
	 */
	public ProcessExecutionException(int exitCode, String message) {
		super(ErrorCode.PROCESS_EXIT_ERROR, message);
		this.exitCode = exitCode;
	}
	
	public int getExitCode() {
		return this.exitCode;
	}

	public String getLogMessage() {
		return String.format("[exitCode %d] [msg %s]", this.exitCode, getMessage());
	}
}
