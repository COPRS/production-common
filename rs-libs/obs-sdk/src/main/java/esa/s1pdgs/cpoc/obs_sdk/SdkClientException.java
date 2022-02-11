package esa.s1pdgs.cpoc.obs_sdk;

/**
 * Base type for all client exceptions thrown by the SDK. This exception is
 * thrown when service could not be contacted for a response, or when client is
 * unable to parse the response from service.
 * 
 * @author Viveris Technologies
 */
public class SdkClientException extends Exception {

    /**
     * Serial version
     */
    private static final long serialVersionUID = -6812572862277601718L;

    /**
     * @see java.lang.Exception#Exception(String)
     */
    public SdkClientException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     */
    public SdkClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
