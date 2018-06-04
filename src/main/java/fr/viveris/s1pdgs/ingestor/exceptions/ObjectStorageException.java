package fr.viveris.s1pdgs.ingestor.exceptions;

/**
 * Exception raised when error occurs when accessing to the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObjectStorageException extends FileRuntimeException {

	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Bucket name
	 */
	protected final String bucket;

	/**
	 * Key in object storage
	 */
	protected final String key;

	/**
	 * Constructor
	 * @param productName
	 * @param key
	 * @param bucket
	 * @param cause
	 */
	public ObjectStorageException(final String productName, final String key, final String bucket,
			final Throwable cause) {
		super(ErrorCode.OBS_ERROR, productName, cause.getMessage(), cause);
		this.key = key;
		this.bucket = bucket;
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[bucket %s] [key %s] [msg %s]", this.bucket, this.key, getMessage());
	}

	/**
	 * @return the bucket
	 */
	public String getBucket() {
		return bucket;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

}
