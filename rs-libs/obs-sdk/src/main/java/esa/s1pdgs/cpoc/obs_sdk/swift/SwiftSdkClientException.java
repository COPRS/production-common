package esa.s1pdgs.cpoc.obs_sdk.swift;

import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;

/**
 * ObsException dedicated to the access via Swift for a given object
 */
public class SwiftSdkClientException extends SdkClientException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = 5199880165421562120L;

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
    public SwiftSdkClientException(final String container, final String key,
            final String message) {
        super(message);
        this.key = key;
        this.container = container;
    }

    /**
     * Constructor
     * 
     * @param container
     * @param key
     * @param cause
     */
    public SwiftSdkClientException(final String container, final String key,
            final String message, final Throwable cause) {
        super(message, cause);
        this.key = key;
        this.container = container;
    }

    /**
     * Constructor
     * 
     * @param container
     * @param cause
     */
    public SwiftSdkClientException(final String container, final String message, final Throwable cause) {
        super(message, cause);
        this.key = null;
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
    	if (null != key) {
    		return String.format(
                "{'container': \"%s\", 'key': \"%s\", 'msg': \"%s\" }", container,
                key, super.getMessage());
    	} else {
    		return String.format(
                    "{'container': \"%s\", 'msg': \"%s\" }", container, super.getMessage());    		
    	}
    }

}
