/**
 * 
 */
package fr.viveris.s1pdgs.common.errors.processing;

/**
 * Exception occurred during job generation
 * 
 * @author Cyrielle Gailliard
 */
public class JobGenMaxNumberCachedJobsReachException
        extends JobGenerationException {

    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * @param taskTable
     * @param message
     */
    public JobGenMaxNumberCachedJobsReachException(final String taskTable,
            final String message) {
        super(taskTable, ErrorCode.MAX_NUMBER_CACHED_JOB_REACH, message);
    }

}
