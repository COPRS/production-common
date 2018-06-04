package fr.viveris.s1pdgs.ingestor.exceptions;

/**
 * Exception which nee to retry file
 *
 */
public abstract class FileRuntimeException extends AbstractFileException {

	private static final long serialVersionUID = -4138493055116873488L;

	/**
	 * 
	 * @param code
	 * @param productName
	 * @param message
	 * @param e
	 */
	public FileRuntimeException(ErrorCode code, final String productName, final String message, final Throwable e) {
		super(code, productName, message, e);
	}

}
