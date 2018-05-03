package fr.viveris.s1pdgs.ingestor.model.exception;

public class IgnoredFileException extends FileTerminatedException {

	private static final long serialVersionUID = 6432844848252714971L;

	private static final String MESSAGE = "File/folder %s will be ignored";

	public IgnoredFileException(String productName, String ignoredName) {
		super(ErrorCode.INGESTOR_IGNORE_FILE, productName, String.format(MESSAGE, ignoredName));
	}

	public IgnoredFileException(String productName, String ignoredName, Throwable cause) {
		super(ErrorCode.INGESTOR_IGNORE_FILE, productName,
				String.format(MESSAGE, ignoredName) + ": " + cause.getMessage(), cause);
	}

	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
