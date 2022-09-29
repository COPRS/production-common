package esa.s1pdgs.cpoc.common.errors.appcatalog;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class AppCatalogJobPatchApiError extends AbstractCodedException {

    /**
     * 
     */
    private static final long serialVersionUID = 8096417675612082535L;

    /**
     * Force
     */
    private final String uri;

    /**
     * object
     */
    private final Object body;

    /**
     * @param category
     * @param message
     */
    public AppCatalogJobPatchApiError(final String uri, final Object body,
            final String message) {
        super(ErrorCode.APPCATALOG_JOB_PATCH_API_ERROR, message);
        this.uri = uri;
        this.body = body;
    }

    /**
     * @param category
     * @param message
     * @param cause
     */
    public AppCatalogJobPatchApiError(final String uri, final Object body,
            final String message, final Throwable cause) {
        super(ErrorCode.APPCATALOG_JOB_PATCH_API_ERROR, message, cause);
        this.uri = uri;
        this.body = body;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the dto
     */
    public Object getBody() {
        return body;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[uri %s] [body %s] [msg %s]", uri, body,
                getMessage());
    }

}
