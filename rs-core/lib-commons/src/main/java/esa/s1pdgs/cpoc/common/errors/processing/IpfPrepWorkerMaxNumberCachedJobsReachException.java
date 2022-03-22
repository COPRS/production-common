/**
 * 
 */
package esa.s1pdgs.cpoc.common.errors.processing;

/**
 * Exception occurred during job generation
 * 
 * @author Cyrielle Gailliard
 */
public class IpfPrepWorkerMaxNumberCachedJobsReachException
        extends IpfPrepWorkerException {

    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * @param taskTable
     * @param message
     */
    public IpfPrepWorkerMaxNumberCachedJobsReachException(final String taskTable,
            final String message) {
        super(taskTable, ErrorCode.MAX_NUMBER_CACHED_JOB_REACH, message);
    }

}
