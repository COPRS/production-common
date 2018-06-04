package fr.viveris.s1pdgs.ingestor.s3.exceptions;

import fr.viveris.s1pdgs.ingestor.exceptions.FileRuntimeException;

public class ObjectStorageException extends FileRuntimeException {

	private static final long serialVersionUID = -3680895691846942569L;

	private static final String MESSAGE = "Object storage failed: %s";

	private String bucket;

	private String key;

	public ObjectStorageException(String productName, String key, String bucket, Throwable cause) {
		super(ErrorCode.OBS_ERROR, productName, String.format(MESSAGE, cause.getMessage()), cause);
		this.key = key;
		this.bucket = bucket;
	}

	@Override
	public String getLogMessage() {
		return String.format("[bucket %s] [key %s] [msg %s]", this.bucket, this.key, getMessage());
	}
}
