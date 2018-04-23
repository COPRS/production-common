package fr.viveris.s1pdgs.jobgenerator.exception;

/**
 * Exception concerning the object storage
 * @author Cyrielle Gailliard
 *
 */
public class ObjectStorageException extends Exception {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = -3680895691846942569L;

	/**
	 * Generic message
	 */
	private static final String MESSAGE = "[key %s] [bucket %s] Object storage failed in bucket: %s";

	private String key;

	/**
	 * Constructor
	 * @param msg
	 * @param key
	 * @param bucket
	 * @param cause
	 */
	public ObjectStorageException(String msg, String key, String bucket, Throwable cause) {
		super(String.format(MESSAGE, bucket, key, msg), cause);
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

}
