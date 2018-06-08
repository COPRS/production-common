package fr.viveris.s1pdgs.ingestor.exceptions;

public abstract class FileTerminatedException extends AbstractFileException {

	private static final long serialVersionUID = -1107106717498506533L;

	public FileTerminatedException(ErrorCode code, String productName, String message) {
		super(code, productName, message);
	}

	public FileTerminatedException(ErrorCode code, String productName, String message, Throwable e) {
		super(code, productName, message, e);
	}

}
