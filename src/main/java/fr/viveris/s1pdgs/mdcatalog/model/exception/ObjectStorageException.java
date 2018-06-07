package fr.viveris.s1pdgs.mdcatalog.model.exception;

/**
 * Exception raised when error occurs when accessing to the object storage
 * 
 * @author Cyrielle Gailliard
 *
 */
public class ObjectStorageException extends AbstractCodedException {
	
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Bucket name
	 */
	protected final String bucket;

	/**
	 * Key in object storage
	 */
	protected final String key;

	public ObjectStorageException(String productName, String key, String bucket, Throwable cause) {
		super(ErrorCode.OBS_ERROR, productName, cause.getMessage(), cause);
		this.key = key;
		this.bucket = bucket;
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

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[bucket %s] [key %s] [msg %s]", this.bucket, this.key, getMessage());
	}
}
