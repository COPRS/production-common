package esa.s1pdgs.cpoc.obs_sdk.s3;

public class S3ObsUnrecoverableException extends S3ObsServiceException {

    public S3ObsUnrecoverableException(final String bucket, final String key, final String message) {
        super(bucket, key, message);
    }

    public S3ObsUnrecoverableException(final String bucket, final String key, final String message, final Throwable cause) {
        super(bucket, key, message, cause);
    }
}
