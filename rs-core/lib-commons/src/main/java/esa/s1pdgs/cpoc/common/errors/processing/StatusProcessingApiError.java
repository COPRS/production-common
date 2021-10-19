package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class StatusProcessingApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;

    /**
     * URI
     */
    private final String uri;

    /**
     * @param uri
     * @param message
     */
    public StatusProcessingApiError(final String uri, final String message) {
        super(ErrorCode.STATUS_PROCESSING_API_ERROR, message);
        this.uri = uri;
    }

    /**
     * @param uri
     * @param message
     * @param cause
     */
    public StatusProcessingApiError(final String uri, final String message,
            final Throwable cause) {
        super(ErrorCode.STATUS_PROCESSING_API_ERROR, message, cause);
        this.uri = uri;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[uri %s] [msg %s]", uri, getMessage());
    }

}
