package fr.viveris.s1pdgs.archives.model.exception;

public class IgnoredFileException extends FileTerminatedException {

	private static final long serialVersionUID = 6432844848252714971L;

	private static final String MESSAGE = "Folder %s will be ignored";

	public IgnoredFileException(String productName) {
		super(String.format(MESSAGE, productName), productName);
	}

	public IgnoredFileException(String productName, Throwable cause) {
		super(String.format(MESSAGE, productName) + ": " + cause.getMessage(), productName, cause);
	}
}
