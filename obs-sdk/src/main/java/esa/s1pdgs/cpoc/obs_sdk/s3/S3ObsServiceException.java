package esa.s1pdgs.cpoc.obs_sdk.s3;

import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;

/**
 * ObsException dedicated to the access via the AmazonS3 API for a given object
 * 
 * @author Viveris Technologies
 */
public class S3ObsServiceException extends ObsServiceException {

    /**
     * Serial version
     */
    private static final long serialVersionUID = 5199880165421562120L;

    /**
     * Key in the object storage of the failed object
     */
    private final String key;

    /**
     * Bucket where the object is stored
     */
    private final String bucket;

    /**
     * Constructor
     * 
     * @param key
     * @param bucket
     * @param cause
     */
    public S3ObsServiceException(final String bucket, final String key,
            final String message) {
        super(message);
        this.key = key;
        this.bucket = bucket;
    }

    /**
     * Constructor
     * 
     * @param key
     * @param bucket
     * @param cause
     */
    public S3ObsServiceException(final String bucket, final String key,
            final String message, final Throwable cause) {
        super(message, cause);
        this.key = key;
        this.bucket = bucket;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @return the bucket
     */
    public String getBucket() {
        return bucket;
    }

    /**
     * Override the getMessage function
     * 
     * @return
     */
    public String getMessage() {
        return String.format(
                "{'bucket': \"%s\", 'key': \"%s\", 'msg': \"%s\" }", bucket,
                key, super.getMessage());
    }

}
