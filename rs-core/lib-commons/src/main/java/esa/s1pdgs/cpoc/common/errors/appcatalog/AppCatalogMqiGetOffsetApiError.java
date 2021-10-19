package esa.s1pdgs.cpoc.common.errors.appcatalog;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class AppCatalogMqiGetOffsetApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;


    /**
     * Force
     */
    private final String uri;

    /**
     * @param category
     * @param message
     */
    public AppCatalogMqiGetOffsetApiError(final String uri, final String message) {
        super(ErrorCode.APPCATALOG_MQI_GET_OFFSET_API_ERROR, message);
        this.uri = uri;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public AppCatalogMqiGetOffsetApiError(
            final String uri, final String message, final Throwable cause) {
        super(ErrorCode.APPCATALOG_MQI_GET_OFFSET_API_ERROR, message, cause);
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
        return String.format("[uri %s] [msg %s]",  uri, getMessage());
    }

}
