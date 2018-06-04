package fr.viveris.s1pdgs.ingestor.exceptions;

public abstract class FileRuntimeException extends AbstractFileException {

	private static final long serialVersionUID = -4138493055116873488L;

	public FileRuntimeException(ErrorCode code, String productName, String message) {
		super(code, productName, message);
	}

	public FileRuntimeException(ErrorCode code, String productName, String message, Throwable e) {
		super(code, productName, message, e);
	}

}
