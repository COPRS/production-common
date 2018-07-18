package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class JobGenMetadataException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * @param category
     * @param message
     */
    public JobGenMetadataException(final String message) {
        super(ErrorCode.JOB_GEN_METADATA_ERROR, message);
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public JobGenMetadataException(final String message, final Throwable cause) {
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
