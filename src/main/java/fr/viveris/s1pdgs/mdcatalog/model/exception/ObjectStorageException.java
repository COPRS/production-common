package fr.viveris.s1pdgs.mdcatalog.model.exception;

public class ObjectStorageException extends FileRuntimeException {
	
	private static final long serialVersionUID = -3680895691846942569L;

	private static final String MESSAGE = "Object storage failed in bucket %s with key %s";
	
	public ObjectStorageException(String productName, String key, String bucket, Throwable cause) {
		super(String.format(MESSAGE, bucket, key), productName, cause);
	}
}
