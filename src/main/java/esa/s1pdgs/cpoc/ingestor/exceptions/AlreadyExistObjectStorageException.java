package esa.s1pdgs.cpoc.ingestor.exceptions;

/**
 * Exception raised when object already exist in object storage
 *
 */
public class AlreadyExistObjectStorageException extends FileTerminatedException {

	private static final long serialVersionUID = -5331517744542218021L;

	/**
	 * Custom message
	 */
	private static final String MESSAGE = "Object already exists in object storage";

	/**
	 * 
	 * @param productName
	 * @param cause
	 */
	public AlreadyExistObjectStorageException(final String productName, final Throwable cause) {
		super(ErrorCode.OBS_ALREADY_EXIST, productName, MESSAGE + ": " + cause.getMessage(), cause);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
