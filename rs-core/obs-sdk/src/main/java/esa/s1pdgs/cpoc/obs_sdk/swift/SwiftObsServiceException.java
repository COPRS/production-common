package esa.s1pdgs.cpoc.obs_sdk.swift;

import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

/**
 * ObsException dedicated to the access via Swift for a given object
 */
public class SwiftObsServiceException extends ObsServiceException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = 6159815121290542160L;

    /**
     * Key in the object storage of the failed object
     */
    private final String key;

    /**
     * Container where the object is stored
     */
    private final String container;

    /**
     * Constructor
     * 
     * @param key
     * @param container
     * @param cause
     */
    public SwiftObsServiceException(final String container, final String key,
            final String message) {
        super(message);
        this.key = key;
        this.container = container;
    }

    /**
     * Constructor
     * 
     * @param key
     * @param container
     * @param cause
     */
    public SwiftObsServiceException(final String container, final String key,
            final String message, final Throwable cause) {
        super(message, cause);
        this.key = key;
        this.container = container;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the container
     */
    public String getContainer() {
        return container;
    }

    /**
     * Override the getMessage function
     * 
     * @return
     */
    public String getMessage() {
        return String.format(
                "{'container': \"%s\", 'key': \"%s\", 'msg': \"%s\" }", container,
                key, super.getMessage());
    }

}
