package fr.viveris.s1pdgs.archives.model.exception;

public class AlreadyExistObjectStorageException extends FileTerminatedException {

	private static final long serialVersionUID = -5331517744542218021L;
	
	private static final String MESSAGE = "Object %s already exists in object storage";

	public AlreadyExistObjectStorageException(String productName) {
		super(String.format(MESSAGE, productName), productName);
	}

	public AlreadyExistObjectStorageException(String productName, Throwable cause) {
		super(String.format(MESSAGE, productName) + ": " + cause.getMessage(), productName, cause);
	}
}
