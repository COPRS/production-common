package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class IpfPrepWorkerMetadataException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * @param category
     * @param message
     */
    public IpfPrepWorkerMetadataException(final String message) {
        super(ErrorCode.JOB_GEN_METADATA_ERROR, message);
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public IpfPrepWorkerMetadataException(final String message, final Throwable cause) {
        super(ErrorCode.JOB_GEN_METADATA_ERROR, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
