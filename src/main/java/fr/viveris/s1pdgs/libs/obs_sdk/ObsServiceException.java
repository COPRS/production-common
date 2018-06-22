package fr.viveris.s1pdgs.libs.obs_sdk;

/**
 * Extension of SdkClientException that represents an error response returned
 * by an Amazon web service. Receiving an exception of this type indicates that
 * the caller's request was correctly transmitted to the service, but for some
 * reason, the service was not able to process it, and returned an error
 * response instead.
 */
public class ObsServiceException extends SdkClientException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = -6812572862277601718L;

    /**
     * @see java.lang.Exception#Exception(String)
     */
    public ObsServiceException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     */
    public ObsServiceException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
