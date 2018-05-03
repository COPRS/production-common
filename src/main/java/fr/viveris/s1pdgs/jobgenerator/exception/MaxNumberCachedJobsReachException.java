/**
 * 
 */
package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception occurred during job generation
 * @author Cyrielle Gailliard
 *
 */
public class MaxNumberCachedJobsReachException extends JobGenerationException {

	private static final long serialVersionUID = -7488001919910076897L;
	
	public MaxNumberCachedJobsReachException(String taskTable, String message) {
		super(taskTable, ErrorCode.MAX_NUMBER_CACHED_JOB_REACH, message);
	}

}
