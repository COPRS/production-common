package esa.s1pdgs.cpoc.common.errors.processing;

/**
 * Exception during task table building
 * 
 * @author Viveris Technologies
 */
public class IpfPrepWorkerBuildTaskTableException extends IpfPrepWorkerException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -4598149920303190179L;

    /**
     * Constructor
     * 
     * @param message
     * @param cause
     * @param taskTable
     */
    public IpfPrepWorkerBuildTaskTableException(final String taskTable,
            final String message, final Throwable cause) {
        super(taskTable, ErrorCode.JOB_GENERATOR_INIT_FAILED, message, cause);
    }

}
