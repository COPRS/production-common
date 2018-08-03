package esa.s1pdgs.cpoc.scaler;

/**
 * 
 */
public class InternalErrorException extends AbstractCodedException {

	private static final long serialVersionUID = 1694474051225086865L;

	/**
	 * 
	 * @param message
	 */
	public InternalErrorException(final String message) {
		super(ErrorCode.INTERNAL_ERROR, message);
	}

	/**
	 * 
	 * @param message
	 * @param cause
	 */
	public InternalErrorException(final String message, final Throwable cause) {
		super(ErrorCode.INTERNAL_ERROR, message,cause);
	}

	/**
	 * 
	 */
	@Override
	public String getLogMessage() {
		return String.format("[msg %s]", getMessage());
	}

}
