package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class MetadataQueryException extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 126226117695671374L;

    /**
     * @param category
     * @param message
     */
    public MetadataQueryException(final String message) {
        super(ErrorCode.METADATA_QUERY_ERROR, message);
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public MetadataQueryException(final String message, final Throwable cause) {
        super(ErrorCode.METADATA_QUERY_ERROR, message, cause);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
