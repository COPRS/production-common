package fr.viveris.s1pdgs.ingestor.model.exception;

public class AlreadyExistObjectStorageException extends FileTerminatedException {

	private static final long serialVersionUID = -5331517744542218021L;
	
	private static final String MESSAGE = "Object already exists in object storage";

	public AlreadyExistObjectStorageException(String productName, Throwable cause) {
		super(ErrorCode.OBS_ALREADY_EXIST, productName, MESSAGE + ": " + cause.getMessage(), cause);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
