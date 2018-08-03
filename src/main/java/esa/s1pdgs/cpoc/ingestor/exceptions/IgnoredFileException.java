package esa.s1pdgs.cpoc.ingestor.exceptions;

/**
 * 
 */
public class IgnoredFileException extends FileTerminatedException {

	private static final long serialVersionUID = 6432844848252714971L;

	/**
	 * 
	 */
	private static final String MESSAGE = "File/folder %s will be ignored";

	/**
	 * 
	 * @param productName
	 * @param ignoredName
	 */
	public IgnoredFileException(final String productName, final String ignoredName) {
		super(ErrorCode.INGESTOR_IGNORE_FILE, productName, String.format(MESSAGE, ignoredName));
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}
}
