package fr.viveris.s1pdgs.mdcatalog.model.exception;

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

	public ObjectStorageException(String productName, String key, String bucket, Throwable cause) {
		super(cause.getMessage(), productName, cause);
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
}
