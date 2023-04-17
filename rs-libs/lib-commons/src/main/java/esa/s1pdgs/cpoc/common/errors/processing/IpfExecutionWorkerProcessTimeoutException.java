package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * Exception occurred during job generation
 * 
 * @author Viveris Technologies
 */
public class IpfExecutionWorkerProcessTimeoutException extends AbstractCodedException {

    /**
     * Serial UID
     */
    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * Constructor
     * 
     * @param message
     */
    public IpfExecutionWorkerProcessTimeoutException(final String message) {
        super(ErrorCode.PROCESS_TIMEOUT, message);
    }

    /**
     * @see AbstractCodedException#getLogMessage()
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }
}
